package io.github.describeadmin.codegen;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate34Test {

    @Test
    void unknownArgumentPrintsErrorAndUsage() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {"spec.yaml", "--bogus"}, out);
        out.flush();
        String text = buf.toString(StandardCharsets.UTF_8);

        assertEquals(1, code);
        assertTrue(text.contains("未知参数: --bogus"), text);
        assertTrue(text.contains("用法:"), text);
        assertTrue(text.contains("--dry-run"), text);
    }
}
