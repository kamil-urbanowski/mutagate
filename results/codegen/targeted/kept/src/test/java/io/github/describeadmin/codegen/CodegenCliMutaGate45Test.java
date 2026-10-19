package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class CodegenCliMutaGate45Test {

    @Test
    void unknownOptionReturnsOneAndPrintsMessage() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {"spec.yaml", "--bogus"}, out);

        out.flush();
        String text = buf.toString(StandardCharsets.UTF_8);
        assertEquals(1, code);
        assertTrue(text.contains("--bogus"));
    }
}
