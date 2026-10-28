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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate28Test {

    @TempDir
    Path tmp;

    private static String capture(String[] args, int[] codeHolder) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        codeHolder[0] = CodegenCli.run(args, ps);
        ps.flush();
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void earlyExitPathsReturnExpectedCodesAndMessages() throws IOException {
        int[] code = new int[1];

        // no args: usage, exit code 1
        String text = capture(new String[0], code);
        assertEquals(1, code[0]);
        assertTrue(text.contains("用法:"));

        // help flags: usage, exit code 0
        text = capture(new String[] {"-h"}, code);
        assertEquals(0, code[0]);
        assertTrue(text.contains("--dry-run"));

        text = capture(new String[] {"--help"}, code);
        assertEquals(0, code[0]);
        assertTrue(text.contains("用法:"));

        // unknown option: reported before spec existence is checked
        Path missing = tmp.resolve("nope.yaml");
        text = capture(new String[] {missing.toString(), "--bogus"}, code);
        assertEquals(1, code[0]);
        assertTrue(text.contains("✗ 未知参数: --bogus"));
        assertTrue(text.contains("用法:"));
        assertFalse(text.contains("Spec 文件不存在"));

        // missing spec: error with absolute path, nothing written to --out
        Path outDir = tmp.resolve("out");
        text = capture(new String[] {missing.toString(), "--out", outDir.toString(), "--force"}, code);
        assertEquals(1, code[0]);
        assertTrue(text.contains("✗ Spec 文件不存在: " + missing.toAbsolutePath()));
        assertFalse(text.contains("用法:"));
        assertFalse(Files.exists(outDir));
    }
}
