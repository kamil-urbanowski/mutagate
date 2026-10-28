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

class CodegenCliMutaGate1Test {

    @TempDir
    Path tmp;

    private int runCli(String[] args, StringBuilder captured) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        int code = CodegenCli.run(args, ps);
        ps.flush();
        captured.append(buf.toString(StandardCharsets.UTF_8));
        return code;
    }

    @Test
    void earlyExitPathsReturnExpectedCodesAndOutput() throws IOException {
        // no args: usage + exit code 1
        StringBuilder noArgs = new StringBuilder();
        assertEquals(1, runCli(new String[0], noArgs));
        assertTrue(noArgs.toString().contains("codegen <spec.yaml>"));
        assertTrue(noArgs.toString().contains("--dry-run"));

        // -h and --help: usage + exit code 0
        StringBuilder shortHelp = new StringBuilder();
        assertEquals(0, runCli(new String[]{"-h"}, shortHelp));
        assertTrue(shortHelp.toString().contains("--force"));
        assertTrue(shortHelp.toString().contains("--frontend-out"));

        StringBuilder longHelp = new StringBuilder();
        assertEquals(0, runCli(new String[]{"--help"}, longHelp));
        assertTrue(longHelp.toString().contains("--layout"));

        // unknown option: reported, usage printed, exit code 1 (checked before spec existence)
        StringBuilder unknown = new StringBuilder();
        Path missingSpec = tmp.resolve("nope.yaml");
        assertEquals(1, runCli(new String[]{missingSpec.toString(), "--bogus"}, unknown));
        assertTrue(unknown.toString().startsWith("\u2717 "));
        assertTrue(unknown.toString().contains("--bogus"));
        assertTrue(unknown.toString().contains("codegen <spec.yaml>"));

        // missing spec file: error with absolute path, exit code 1, nothing written
        Path outDir = tmp.resolve("out");
        StringBuilder missing = new StringBuilder();
        assertEquals(1, runCli(new String[]{missingSpec.toString(), "--out", outDir.toString()}, missing));
        assertTrue(missing.toString().startsWith("\u2717 "));
        assertTrue(missing.toString().contains(missingSpec.toAbsolutePath().toString()));
        assertFalse(missing.toString().contains("codegen <spec.yaml>"));
        assertFalse(Files.exists(outDir));
    }
}
