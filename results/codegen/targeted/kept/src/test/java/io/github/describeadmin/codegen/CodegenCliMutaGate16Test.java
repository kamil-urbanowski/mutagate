package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate16Test {

    @TempDir
    Path tmp;

    private static final class Result {
        int code;
        String output;
    }

    private static Result invoke(String... args) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        Result r = new Result();
        r.code = CodegenCli.run(args, ps);
        ps.flush();
        r.output = buf.toString(StandardCharsets.UTF_8);
        return r;
    }

    @Test
    void helpFlagsAndNormalArgsAreDistinguished() throws IOException {
        Result none = invoke();
        assertEquals(1, none.code);
        assertTrue(none.output.contains("用法:"));

        Result shortHelp = invoke("-h");
        assertEquals(0, shortHelp.code);
        assertTrue(shortHelp.output.contains("用法:"));

        Result longHelp = invoke("--help");
        assertEquals(0, longHelp.code);
        assertTrue(longHelp.output.contains("用法:"));

        Path missing = tmp.resolve("does-not-exist.yaml");
        Result normal = invoke(missing.toString());
        assertEquals(1, normal.code);
        assertTrue(normal.output.contains("Spec 文件不存在"));
        assertFalse(normal.output.contains("用法:"));
    }
}
