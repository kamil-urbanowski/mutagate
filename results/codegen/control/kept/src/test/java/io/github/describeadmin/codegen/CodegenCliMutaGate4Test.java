package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate4Test {

    private static String capture(int[] codeHolder, String... args) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        codeHolder[0] = CodegenCli.run(args, ps);
        ps.flush();
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void earlyExitPathsReturnExpectedCodesAndMessages(@TempDir Path tmp) throws IOException {
        int[] code = new int[1];

        // no args: usage, exit code 1
        String text = capture(code);
        assertEquals(1, code[0]);
        assertTrue(text.contains("用法:"));
        assertTrue(text.contains("--dry-run"));

        // --help: usage, exit code 0
        text = capture(code, "--help");
        assertEquals(0, code[0]);
        assertTrue(text.contains("用法:"));

        // -h: usage, exit code 0
        text = capture(code, "-h");
        assertEquals(0, code[0]);
        assertTrue(text.contains("用法:"));

        // unknown option: error + usage, exit code 1 (spec existence not yet checked)
        text = capture(code, tmp.resolve("whatever.yaml").toString(), "--bogus");
        assertEquals(1, code[0]);
        assertTrue(text.contains("✗ 未知参数: --bogus"));
        assertTrue(text.contains("用法:"));

        // missing spec file: error with absolute path, exit code 1, no usage, nothing written
        Path missing = tmp.resolve("missing.yaml");
        Path outDir = tmp.resolve("out");
        text = capture(code, missing.toString(), "--out", outDir.toString(), "--force");
        assertEquals(1, code[0]);
        assertTrue(text.contains("✗ Spec 文件不存在: " + missing.toAbsolutePath()));
        assertFalse(text.contains("用法:"));
        assertFalse(Files.exists(outDir));

        try (Stream<Path> children = Files.list(tmp)) {
            assertEquals(0, children.count());
        }
    }
}
