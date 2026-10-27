package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    Path tmp;

    @Test
    void missingSpecFileReturnsOneAndReportsItWithoutWritingAnything() throws Exception {
        Path missing = tmp.resolve("does-not-exist.yaml");
        Path outDir = tmp.resolve("out");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {missing.toString(), "--out", outDir.toString()}, out);
        out.flush();
        String text = buffer.toString(StandardCharsets.UTF_8);

        assertEquals(1, code);
        assertTrue(text.contains("Spec 文件不存在"), text);
        assertTrue(text.contains(missing.toAbsolutePath().toString()), text);
        assertTrue(!Files.exists(outDir), "nothing should be generated");
    }
}
