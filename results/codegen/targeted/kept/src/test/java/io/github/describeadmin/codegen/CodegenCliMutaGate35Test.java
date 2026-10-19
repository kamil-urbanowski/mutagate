package io.github.describeadmin.codegen;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate35Test {

    @Test
    void missingSpecFilePrintsErrorAndReturnsOne() throws Exception {
        Path dir = Files.createTempDirectory("codegen-cli-test");
        Path missing = dir.resolve("does-not-exist.yaml");

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {missing.toString()}, out);
        out.flush();

        String text = buf.toString(StandardCharsets.UTF_8);
        assertEquals(1, code);
        assertTrue(text.contains("✗ Spec 文件不存在: "), text);
        assertTrue(text.contains("does-not-exist.yaml"), text);

        Files.deleteIfExists(dir);
    }
}
