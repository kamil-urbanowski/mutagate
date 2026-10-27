package io.github.describeadmin.codegen;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate46Test {

    @Test
    void missingSpecFileReturnsOneAndReportsError() throws Exception {
        Path dir = Files.createTempDirectory("codegen-cli-test");
        Path missing = dir.resolve("does-not-exist.yaml");

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {missing.toString()}, out);

        String text = buf.toString(StandardCharsets.UTF_8);
        assertEquals(1, code);
        assertTrue(text.contains("Spec 文件不存在"), text);

        Files.deleteIfExists(dir);
    }
}
