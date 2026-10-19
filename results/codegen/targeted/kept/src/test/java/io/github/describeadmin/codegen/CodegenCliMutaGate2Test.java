package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate2Test {

    @TempDir
    Path tmp;

    @Test
    void displayReturnsRelativePathInsideOutDirAndFullPathOutside() throws Exception {
        Method m = CodegenCli.class.getDeclaredMethod("display", Path.class, Path.class);
        m.setAccessible(true);

        Path outDir = tmp.resolve("out");
        Path inside = outDir.resolve("src/main/java/A.java");
        assertEquals("src/main/java/A.java", m.invoke(null, outDir, inside));

        Path outside = tmp.resolve("other").resolve("api.ts");
        assertEquals(outside.toString().replace('\\', '/'), m.invoke(null, outDir, outside));
    }
}
