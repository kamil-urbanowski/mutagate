package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate12Test {

    @TempDir
    Path tempDir;

    @Test
    void runWithOnlySpecArgumentReachesMissingSpecCheck() throws Exception {
        Path missing = tempDir.resolve("does-not-exist.yaml");
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {missing.toString()}, out);

        String printed = buffer.toString(StandardCharsets.UTF_8);
        assertEquals(1, code);
        assertTrue(printed.contains("Spec"), printed);
        assertTrue(printed.contains("does-not-exist.yaml"), printed);
    }
}
