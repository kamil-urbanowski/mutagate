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

class CodegenCliMutaGate46Test {

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

        // no args: usage, exit code 1
        String noArgs = capture(new String[0], code);
        assertEquals(1, code[0]);
        assertTrue(noArgs.contains("用法:"));
        assertTrue(noArgs.contains("--dry-run"));

        // --help and -h as first arg: usage, exit code 0
        String help = capture(new String[] {"--help"}, code);
        assertEquals(0, code[0]);
        assertTrue(help.contains("用法:"));

        String shortHelp = capture(new String[] {"-h"}, code);
        assertEquals(0, code[0]);
        assertTrue(shortHelp.contains("用法:"));

        // unknown option: message + usage, exit code 1 (checked before spec existence)
        String unknown = capture(new String[] {"whatever.yaml", "--bogus"}, code);
        assertEquals(1, code[0]);
        assertTrue(unknown.contains("未知参数: --bogus"));
        assertTrue(unknown.contains("用法:"));

        // --help is only special as first argument
        String helpLate = capture(new String[] {"whatever.yaml", "--help"}, code);
        assertEquals(1, code[0]);
        assertTrue(helpLate.contains("未知参数: --help"));

        // missing spec file: error with absolute path, exit 1, nothing written
        Path missing = tmp.resolve("missing.yaml");
        Path outDir = tmp.resolve("out");
        String missingOut = capture(
                new String[] {missing.toString(), "--out", outDir.toString()}, code);
        assertEquals(1, code[0]);
        assertTrue(missingOut.contains("Spec 文件不存在: " + missing.toAbsolutePath()));
        assertFalse(missingOut.contains("用法:"));
        assertFalse(Files.exists(outDir));
    }
}
