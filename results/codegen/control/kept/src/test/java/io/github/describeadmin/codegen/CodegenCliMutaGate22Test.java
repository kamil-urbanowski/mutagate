package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate22Test {

    @TempDir
    Path tmp;

    private static String capture(int[] code, String... args) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        code[0] = CodegenCli.run(args, ps);
        ps.flush();
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void earlyExitPathsReturnExpectedCodesAndMessages() throws Exception {
        int[] code = new int[1];

        // no args: usage, exit code 1
        String noArgs = capture(code);
        assertEquals(1, code[0]);
        assertTrue(noArgs.contains("用法:"));

        // help flags: usage, exit code 0
        String help = capture(code, "--help");
        assertEquals(0, code[0]);
        assertTrue(help.contains("用法:"));
        assertTrue(help.contains("--dry-run"));

        String shortHelp = capture(code, "-h");
        assertEquals(0, code[0]);
        assertTrue(shortHelp.contains("用法:"));

        // unknown option: error + usage, exit code 1
        Path missing = tmp.resolve("missing.yaml");
        String unknown = capture(code, missing.toString(), "--bogus");
        assertEquals(1, code[0]);
        assertTrue(unknown.contains("✗ 未知参数: --bogus"));
        assertTrue(unknown.contains("用法:"));

        // missing spec file: error with absolute path, exit code 1
        String notFound = capture(code, missing.toString(), "--out", tmp.resolve("out").toString());
        assertEquals(1, code[0]);
        assertTrue(notFound.contains("✗ Spec 文件不存在: " + missing.toAbsolutePath()));
        assertFalse(notFound.contains("用法:"));

        // nothing was written anywhere
        try (Stream<Path> s = Files.list(tmp)) {
            assertEquals(0, s.count());
        }
    }
}
