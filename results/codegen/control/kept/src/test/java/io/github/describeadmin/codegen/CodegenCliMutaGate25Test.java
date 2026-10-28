package io.github.describeadmin.codegen;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate25Test {

    private static String capture(int[] codeHolder, String... args) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        codeHolder[0] = CodegenCli.run(args, ps);
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
        assertTrue(noArgs.contains("--dry-run"));

        // -h and --help: usage, exit code 0
        String shortHelp = capture(code, "-h");
        assertEquals(0, code[0]);
        assertTrue(shortHelp.contains("用法:"));

        String longHelp = capture(code, "--help");
        assertEquals(0, code[0]);
        assertTrue(longHelp.contains("用法:"));

        // unknown option: message + usage, exit code 1 (checked before spec existence)
        String unknown = capture(code, "whatever.yaml", "--bogus");
        assertEquals(1, code[0]);
        assertTrue(unknown.contains("✗ 未知参数: --bogus"));
        assertTrue(unknown.contains("用法:"));

        // missing spec file: error with absolute path, exit code 1, nothing written
        Path tmp = Files.createTempDirectory("codegen-cli-test");
        try {
            Path missing = tmp.resolve("no-such-spec.yaml");
            Path outDir = tmp.resolve("out");
            String missingOut = capture(code, missing.toString(), "--out", outDir.toString());
            assertEquals(1, code[0]);
            assertTrue(missingOut.contains("✗ Spec 文件不存在: " + missing.toAbsolutePath()));
            assertFalse(missingOut.contains("用法:"));
            assertFalse(Files.exists(outDir));
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
