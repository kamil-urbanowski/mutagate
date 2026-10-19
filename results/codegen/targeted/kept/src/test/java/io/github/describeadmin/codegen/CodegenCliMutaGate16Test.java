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

class CodegenCliMutaGate16Test {

    private static final String USAGE_MARKER = "用法:";

    private static String capture(String[] args, int[] codeHolder) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8)) {
            codeHolder[0] = CodegenCli.run(args, ps);
        }
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void helpHandlingAndNonHelpArgument() throws Exception {
        int[] code = new int[1];

        String noArgs = capture(new String[0], code);
        assertEquals(1, code[0]);
        assertTrue(noArgs.contains(USAGE_MARKER));

        String shortHelp = capture(new String[] {"-h"}, code);
        assertEquals(0, code[0]);
        assertTrue(shortHelp.contains(USAGE_MARKER));

        String longHelp = capture(new String[] {"--help"}, code);
        assertEquals(0, code[0]);
        assertTrue(longHelp.contains(USAGE_MARKER));

        Path dir = Files.createTempDirectory("codegen-cli-test");
        try {
            Path missing = dir.resolve("does-not-exist.yaml");
            String other = capture(new String[] {missing.toString()}, code);
            assertEquals(1, code[0]);
            assertTrue(other.contains("Spec 文件不存在"));
            assertFalse(other.contains(USAGE_MARKER));
        } finally {
            Files.deleteIfExists(dir);
        }
    }
}
