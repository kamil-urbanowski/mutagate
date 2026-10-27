package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class CodegenCliMutaGate2Test {

    @Test
    void displayReturnsRelativePathInsideOutDirAndFullPathOutside() throws Exception {
        Method display = CodegenCli.class.getDeclaredMethod("display", Path.class, Path.class);
        display.setAccessible(true);

        Path outDir = Path.of("base-out").toAbsolutePath();

        Path inside = outDir.resolve("src/main/java/Foo.java");
        String insideResult = (String) display.invoke(null, outDir, inside);
        assertEquals("src/main/java/Foo.java", insideResult);

        Path outside = outDir.resolve("../other-frontend/src/api/foo.ts");
        String outsideResult = (String) display.invoke(null, outDir, outside);
        assertEquals(outside.toString().replace('\\', '/'), outsideResult);
    }
}
