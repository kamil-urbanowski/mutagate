package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class CodegenCliMutaGate32Test {

    @Test
    void helpPrintsUsageAndReturnsZero() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {"--help"}, out);
        out.flush();

        String text = buffer.toString(StandardCharsets.UTF_8);
        assertEquals(0, code);
        assertTrue(text.contains("用法:"), text);
        assertTrue(text.contains("--dry-run"), text);
    }
}
