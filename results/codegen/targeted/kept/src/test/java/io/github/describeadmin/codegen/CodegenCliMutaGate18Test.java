package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CodegenCliMutaGate18Test {

    private static final class Result {
        final int code;
        final String output;

        Result(int code, String output) {
            this.code = code;
            this.output = output;
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
    void helpAndEmptyArgsHandling() throws Exception {
        Result none = invoke();
        assertEquals(1, none.code);
        assertTrue(none.output.contains("describeadmin codegen"));

        Result shortHelp = invoke("-h");
        assertEquals(0, shortHelp.code);
        assertTrue(shortHelp.output.contains("describeadmin codegen"));

        Result longHelp = invoke("--help");
        assertEquals(0, longHelp.code);
        assertTrue(longHelp.output.contains("describeadmin codegen"));

        Result missing = invoke("definitely-missing-spec-file-xyz.yaml");
        assertEquals(1, missing.code);
        assertTrue(missing.output.contains("Spec 文件不存在"));
        assertFalse(missing.output.contains("describeadmin codegen"));
    }
}
