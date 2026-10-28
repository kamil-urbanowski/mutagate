package io.github.describeadmin.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate13Test {

    @TempDir
    Path tmp;

    private static String capture(String[] args, int[] code) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(bos, true, StandardCharsets.UTF_8)) {
            code[0] = CodegenCli.run(args, ps);
        }
        return bos.toString(StandardCharsets.UTF_8);
    }

    @Test
    void argumentHandlingBeforeAnyGeneration() throws Exception {
        int[] code = new int[1];

        // no args: usage, exit code 1
        String noArgs = capture(new String[0], code);
        assertEquals(1, code[0]);
        assertTrue(noArgs.contains("用法:"));
        assertTrue(noArgs.contains("--dry-run"));

        // -h and --help: same usage, exit code 0
        String shortHelp = capture(new String[]{"-h"}, code);
        assertEquals(0, code[0]);
        assertEquals(noArgs, shortHelp);

        String longHelp = capture(new String[]{"--help"}, code);
        assertEquals(0, code[0]);
        assertEquals(noArgs, longHelp);

        // unknown option: reported (before spec existence is checked), usage printed, exit 1
        Path missing = tmp.resolve("nope.yaml");
        String unknown = capture(new String[]{missing.toString(), "--bogus"}, code);
        assertEquals(1, code[0]);
        assertTrue(unknown.contains("✗ 未知参数: --bogus"));
        assertTrue(unknown.contains("用法:"));
        assertFalse(unknown.contains("Spec 文件不存在"));

        // missing spec file: error with absolute path, exit 1, nothing written
        Path outDir = tmp.resolve("out");
        String notFound = capture(new String[]{missing.toString(), "--out", outDir.toString(), "--force"}, code);
        assertEquals(1, code[0]);
        assertTrue(notFound.contains("✗ Spec 文件不存在: " + missing.toAbsolutePath()));
        assertFalse(notFound.contains("用法:"));
        assertFalse(Files.exists(outDir));
        try (Stream<Path> children = Files.list(tmp)) {
            assertEquals(0, children.count());
        }
    }
}
