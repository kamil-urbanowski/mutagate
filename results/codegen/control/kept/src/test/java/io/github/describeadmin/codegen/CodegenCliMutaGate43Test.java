package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate43Test {

    @TempDir
    Path tmp;

    private static String capture(String[] args, int[] codeHolder) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        codeHolder[0] = CodegenCli.run(args, ps);
        ps.flush();
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void earlyExitPathsReturnExpectedCodesAndMessages() throws Exception {
        int[] code = new int[1];

        // No args: usage and exit code 1
        String noArgs = capture(new String[0], code);
        assertEquals(1, code[0]);
        assertTrue(noArgs.contains("用法:"));

        // Help flags: usage and exit code 0
        String help = capture(new String[] {"--help"}, code);
        assertEquals(0, code[0]);
        assertTrue(help.contains("用法:"));

        String shortHelp = capture(new String[] {"-h"}, code);
        assertEquals(0, code[0]);
        assertTrue(shortHelp.contains("--dry-run"));

        // Unknown option: error line, usage, exit code 1 (before spec existence check)
        String unknown = capture(new String[] {"spec.yaml", "--bogus"}, code);
        assertEquals(1, code[0]);
        assertTrue(unknown.contains("✗ 未知参数: --bogus"));
        assertTrue(unknown.contains("用法:"));

        // Missing spec file: error message, exit code 1, nothing written
        Path missing = tmp.resolve("nope.yaml");
        Path outDir = tmp.resolve("out");
        String missingOut = capture(
                new String[] {missing.toString(), "--out", outDir.toString(), "--force"}, code);
        assertEquals(1, code[0]);
        assertTrue(missingOut.contains("✗ Spec 文件不存在: " + missing.toAbsolutePath()));
        assertFalse(missingOut.contains("完成："));
        assertFalse(Files.exists(outDir));
    }
}
