package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate3Test {

    @TempDir
    Path tempDir;

    @Test
    void displayFallsBackToTargetStringWhenRelativizeThrows() throws Exception {
        Path zip = tempDir.resolve("t.zip");
        URI uri = URI.create("jar:" + zip.toUri());

        try (FileSystem zipFs = FileSystems.newFileSystem(uri, Map.of("create", "true"))) {
            // Path from a different provider: relativize on the default-fs path
            // throws ProviderMismatchException (an IllegalArgumentException).
            Path target = zipFs.getPath("/a/b\\c.txt");
            Path outDir = tempDir.resolve("out");

            Method display = CodegenCli.class.getDeclaredMethod("display", Path.class, Path.class);
            display.setAccessible(true);
            String result = (String) display.invoke(null, outDir, target);

            assertEquals(target.toString().replace('\\', '/'), result);
            assertEquals("/a/b/c.txt", result);
        }
    }
}
