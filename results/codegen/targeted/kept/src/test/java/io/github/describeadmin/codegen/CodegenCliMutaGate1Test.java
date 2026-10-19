package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class CodegenCliMutaGate1Test {

    @Test
    void displayReturnsRelativeInsideOutDirAndTargetPathOutside() throws Exception {
        Method display = CodegenCli.class.getDeclaredMethod("display", Path.class, Path.class);
        display.setAccessible(true);

        Path base = Path.of("codegen-display-base").toAbsolutePath().normalize();
        Path outDir = base.resolve("out");

        Path inside = outDir.resolve("src").resolve("main").resolve("A.java");
        String insideResult = (String) display.invoke(null, outDir, inside);
        assertEquals("src/main/A.java", insideResult);

        Path outside = base.resolve("other").resolve("f.ts");
        String outsideResult = (String) display.invoke(null, outDir, outside);
        assertEquals(outside.toString().replace('\\', '/'), outsideResult);
    }
}
