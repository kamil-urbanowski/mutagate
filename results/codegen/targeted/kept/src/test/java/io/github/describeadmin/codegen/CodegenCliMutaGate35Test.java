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

class CodegenCliMutaGate35Test {

    @Test
    void missingSpecFilePrintsErrorAndReturnsOne(@TempDir Path tmp) throws Exception {
        Path missing = tmp.resolve("does-not-exist.yaml");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {missing.toString()}, out);
        out.flush();

        String output = buf.toString(StandardCharsets.UTF_8);
        assertEquals(1, code);
        assertTrue(output.contains("✗ Spec 文件不存在: "), output);
        assertTrue(output.contains(missing.toAbsolutePath().toString()), output);
    }
}
