package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CodegenCliMutaGate16Test {

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
    void argumentHandlingBeforeSpecIsLoaded() throws Exception {
        // no args: usage printed, exit code 1
        Result none = invoke();
        assertEquals(1, none.code);
        assertTrue(none.text.contains("codegen <spec.yaml>"));

        // help flags: usage printed, exit code 0
        Result shortHelp = invoke("-h");
        assertEquals(0, shortHelp.code);
        assertTrue(shortHelp.text.contains("codegen <spec.yaml>"));

        Result longHelp = invoke("--help");
        assertEquals(0, longHelp.code);
        assertTrue(longHelp.text.contains("--dry-run"));

        // unknown option: reported, usage printed, exit code 1 (before spec existence is checked)
        Result unknown = invoke("missing-spec-for-test.yaml", "--bogus");
        assertEquals(1, unknown.code);
        assertTrue(unknown.text.contains("--bogus"));
        assertTrue(unknown.text.contains("codegen <spec.yaml>"));

        // missing spec file: reported with its path, exit code 1, no usage text
        Result missing = invoke("definitely-missing-spec-xyz.yaml", "--dry-run");
        assertEquals(1, missing.code);
        assertTrue(missing.text.contains("definitely-missing-spec-xyz.yaml"));
        assertFalse(missing.text.contains("codegen <spec.yaml>"));
    }
}
