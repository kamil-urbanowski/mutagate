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

class CodegenCliMutaGate22Test {

    @TempDir
    Path tempDir;

    @Test
    void missingSpecFileReturnsOneAndReportsPathWithoutGeneratingAnything() throws Exception {
        Path missing = tempDir.resolve("does-not-exist.yaml");
        Path outDir = tempDir.resolve("out");

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {missing.toString(), "--out", outDir.toString()}, out);
        out.flush();
        String text = buf.toString(StandardCharsets.UTF_8);

        assertEquals(1, code);
        assertTrue(text.contains(missing.toAbsolutePath().toString()), text);
        assertFalse(Files.exists(outDir));
    }
}
