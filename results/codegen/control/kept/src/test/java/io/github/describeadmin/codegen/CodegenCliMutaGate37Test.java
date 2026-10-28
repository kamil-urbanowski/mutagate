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

class CodegenCliMutaGate37Test {

    @TempDir
    Path tmp;

    private static String capture(String[] args, int[] code) throws Exception {
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
        String noArgs = capture(new String[0], code);
        assertEquals(1, code[0]);
        assertTrue(noArgs.contains("用法:"));
        assertTrue(noArgs.contains("--dry-run"));

        // --help: usage, exit code 0
        String help = capture(new String[] {"--help"}, code);
        assertEquals(0, code[0]);
        assertTrue(help.contains("用法:"));

        // -h: usage, exit code 0
        String shortHelp = capture(new String[] {"-h", "ignored"}, code);
        assertEquals(0, code[0]);
        assertTrue(shortHelp.contains("用法:"));

        // unknown option: error + usage, exit code 1 (checked before spec existence)
        String unknown = capture(new String[] {"spec.yaml", "--bogus"}, code);
        assertEquals(1, code[0]);
        assertTrue(unknown.contains("未知参数: --bogus"));
        assertTrue(unknown.contains("用法:"));

        // missing spec file: error, exit code 1, nothing written to out dir
        Path missing = tmp.resolve("nope.yaml");
        Path outDir = tmp.resolve("out");
        String missingOut = capture(
                new String[] {missing.toString(), "--out", outDir.toString()}, code);
        assertEquals(1, code[0]);
        assertTrue(missingOut.contains("Spec 文件不存在"));
        assertTrue(missingOut.contains("nope.yaml"));
        assertFalse(missingOut.contains("用法:"));
        assertFalse(Files.exists(outDir));
    }
}
