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

class CodegenCliMutaGate19Test {

    @TempDir
    Path tmp;

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
    void earlyExitPathsReturnExpectedCodesAndMessages() throws Exception {
        // no args: usage printed, exit code 1
        Result none = invoke();
        assertEquals(1, none.code);
        assertTrue(none.text.contains("用法:"));
        assertTrue(none.text.contains("--dry-run"));

        // -h and --help: usage printed, exit code 0
        Result shortHelp = invoke("-h");
        assertEquals(0, shortHelp.code);
        assertTrue(shortHelp.text.contains("用法:"));
        Result longHelp = invoke("--help");
        assertEquals(0, longHelp.code);
        assertTrue(longHelp.text.contains("--frontend-out"));

        // unknown option is rejected before the spec file is checked for existence
        Path missing = tmp.resolve("nope.yaml");
        Result unknown = invoke(missing.toString(), "--bogus");
        assertEquals(1, unknown.code);
        assertTrue(unknown.text.contains("✗ 未知参数: --bogus"));
        assertTrue(unknown.text.contains("用法:"));
        assertFalse(unknown.text.contains("Spec 文件不存在"));

        // valid options but missing spec file: error with absolute path, nothing written
        Path outDir = tmp.resolve("out");
        Result noSpec = invoke(missing.toString(), "--out", outDir.toString(), "--dry-run", "--force");
        assertEquals(1, noSpec.code);
        assertTrue(noSpec.text.contains("✗ Spec 文件不存在: " + missing.toAbsolutePath()));
        assertFalse(noSpec.text.contains("用法:"));
        assertFalse(Files.exists(outDir));
    }
}
