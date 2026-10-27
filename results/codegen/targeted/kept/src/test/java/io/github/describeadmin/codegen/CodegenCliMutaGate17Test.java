package io.github.describeadmin.codegen;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate17Test {

    private static final class Result {
        final int code;
        final String text;

        Result(int code, String text) {
            this.code = code;
            this.text = text;
        }
    }

    private static Result invoke(String... args) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        int code = CodegenCli.run(args, ps);
        ps.flush();
        return new Result(code, buf.toString(StandardCharsets.UTF_8));
    }

    @Test
    void helpAndEmptyArgsHandledBeforeSpecLookup() throws Exception {
        Result empty = invoke();
        assertEquals(1, empty.code);
        assertTrue(empty.text.contains("用法:"));

        Result shortHelp = invoke("-h");
        assertEquals(0, shortHelp.code);
        assertTrue(shortHelp.text.contains("用法:"));

        Result longHelp = invoke("--help");
        assertEquals(0, longHelp.code);
        assertTrue(longHelp.text.contains("用法:"));

        Result missing = invoke("definitely-not-existing-spec-file-17.yaml");
        assertEquals(1, missing.code);
        assertTrue(missing.text.contains("Spec 文件不存在"));
        assertFalse(missing.text.contains("用法:"));
    }
}
