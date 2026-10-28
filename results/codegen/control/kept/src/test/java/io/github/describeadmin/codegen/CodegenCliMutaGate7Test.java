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

class CodegenCliMutaGate7Test {

    private static String capture(String[] args, int[] codeHolder) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        codeHolder[0] = CodegenCli.run(args, ps);
        ps.flush();
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void earlyExitPathsReturnExpectedCodesAndMessages(@TempDir Path tmp) throws Exception {
        int[] code = new int[1];

        // no args: usage, exit code 1
        String text = capture(new String[0], code);
        assertEquals(1, code[0]);
        assertTrue(text.contains("用法:"));
        assertTrue(text.contains("codegen <spec.yaml>"));

        // -h: usage, exit code 0
        text = capture(new String[] {"-h"}, code);
        assertEquals(0, code[0]);
        assertTrue(text.contains("用法:"));

        // --help: usage, exit code 0
        text = capture(new String[] {"--help"}, code);
        assertEquals(0, code[0]);
        assertTrue(text.contains("--dry-run"));

        // unknown option: error line plus usage, exit code 1
        text = capture(new String[] {"spec.yaml", "--bogus"}, code);
        assertEquals(1, code[0]);
        assertTrue(text.contains("✗ 未知参数: --bogus"));
        assertTrue(text.contains("用法:"));

        // missing spec file: error with absolute path, exit code 1, nothing written
        Path missing = tmp.resolve("nope.yaml");
        Path outDir = tmp.resolve("out");
        text = capture(new String[] {missing.toString(), "--out", outDir.toString(), "--force"}, code);
        assertEquals(1, code[0]);
        assertTrue(text.contains("✗ Spec 文件不存在: " + missing.toAbsolutePath()));
        assertFalse(text.contains("用法:"));
        assertFalse(Files.exists(outDir));
        try (Stream<Path> children = Files.list(tmp)) {
            assertEquals(0, children.count());
        }
    }
}
