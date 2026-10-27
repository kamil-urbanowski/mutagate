package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate1Test {

    @TempDir
    Path tmp;

    @Test
    void displayUsesRelativePathInsideOutDirAndFullPathOutside() throws Exception {
        Method display = CodegenCli.class.getDeclaredMethod("display", Path.class, Path.class);
        display.setAccessible(true);

        Path outDir = tmp.resolve("out");

        Path inside = outDir.resolve("src").resolve("a.txt");
        String insideResult = (String) display.invoke(null, outDir, inside);
        assertEquals("src/a.txt", insideResult);

        Path outside = tmp.resolve("other").resolve("x.txt");
        String outsideResult = (String) display.invoke(null, outDir, outside);
        assertEquals(outside.toString().replace('\\', '/'), outsideResult);
    }
}
