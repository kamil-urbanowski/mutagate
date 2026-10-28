package io.github.describeadmin.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate40Test {

    @TempDir
    Path tmp;

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
        int code;
        try (PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8)) {
            code = CodegenCli.run(args, ps);
        }
        return new Result(code, buf.toString(StandardCharsets.UTF_8));
    }

    @Test
    void earlyExitPathsReturnExpectedCodesAndMessages() throws Exception {
        // no args: usage, exit code 1
        Result none = invoke();
        assertEquals(1, none.code);
        assertTrue(none.output.contains("用法:"));
        assertTrue(none.output.contains("--dry-run"));

        // help flags: usage, exit code 0
        Result shortHelp = invoke("-h");
        assertEquals(0, shortHelp.code);
        assertTrue(shortHelp.output.contains("用法:"));

        Result longHelp = invoke("--help");
        assertEquals(0, longHelp.code);
        assertTrue(longHelp.output.contains("用法:"));

        // unknown option: error message plus usage, exit code 1
        Result unknown = invoke("whatever.yaml", "--bogus");
        assertEquals(1, unknown.code);
        assertTrue(unknown.output.contains("✗ 未知参数: --bogus"));
        assertTrue(unknown.output.contains("用法:"));

        // missing spec file: error with absolute path, exit code 1, nothing written
        Path missing = tmp.resolve("nope.yaml");
        Path outDir = tmp.resolve("out");
        Result missingSpec = invoke(missing.toString(), "--out", outDir.toString());
        assertEquals(1, missingSpec.code);
        assertTrue(missingSpec.output.contains("✗ Spec 文件不存在: " + missing.toAbsolutePath()));
        assertFalse(missingSpec.output.contains("用法:"));
        assertFalse(Files.exists(outDir));
    }
}
