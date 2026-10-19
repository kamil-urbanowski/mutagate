package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class CodegenCliMutaGate20Test {

    @Test
    void unknownOptionAfterSpecIsRejectedBeforeSpecIsChecked() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {"nonexistent-spec-file.yaml", "--bogus"}, out);
        out.flush();
        String text = buffer.toString(StandardCharsets.UTF_8);

        assertEquals(1, code);
        assertTrue(text.contains("未知参数: --bogus"), text);
        assertFalse(text.contains("Spec 文件不存在"), text);
    }
}
