package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate17Test {

    @TempDir
    Path tempDir;

    private static String capture(String[] args, int[] codeHolder, int idx) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        codeHolder[idx] = CodegenCli.run(args, ps);
        ps.flush();
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void helpAndEmptyArgsHandledBeforeSpecLoading() throws Exception {
        int[] codes = new int[4];

        String noArgs = capture(new String[0], codes, 0);
        String shortHelp = capture(new String[] {"-h"}, codes, 1);
        String longHelp = capture(new String[] {"--help"}, codes, 2);
        String missing = capture(new String[] {tempDir.resolve("missing.yaml").toString()}, codes, 3);

        assertEquals(1, codes[0]);
        assertTrue(noArgs.contains("用法:"));

        assertEquals(0, codes[1]);
        assertTrue(shortHelp.contains("用法:"));

        assertEquals(0, codes[2]);
        assertTrue(longHelp.contains("用法:"));

        assertEquals(1, codes[3]);
        assertTrue(missing.contains("Spec 文件不存在"));
        assertFalse(missing.contains("用法:"));
    }
}
