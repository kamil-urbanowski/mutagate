package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate3Test {

    @TempDir
    Path tmp;

    @Test
    void displayFallsBackToTargetPathWhenRelativizeThrowsIllegalArgument() throws Exception {
        Path zip = tmp.resolve("other.zip");
        try (FileSystem zfs = FileSystems.newFileSystem(zip, Map.of("create", "true"))) {
            // A path from a different provider makes relativize throw
            // ProviderMismatchException, a subclass of IllegalArgumentException.
            Path target = zfs.getPath("/a/b.txt");
            Path outDir = tmp.resolve("out");

            Method display = CodegenCli.class.getDeclaredMethod("display", Path.class, Path.class);
            display.setAccessible(true);
            String result = (String) display.invoke(null, outDir, target);

            assertEquals("/a/b.txt", result);
        }
    }
}
