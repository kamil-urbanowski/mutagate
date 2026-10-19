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
    void missingSpecFileReturnsOne() throws Exception {
        Path dir = Files.createTempDirectory("codegen-missing-spec");
        Path spec = dir.resolve("does-not-exist.yaml");

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {spec.toString()}, out);

        assertEquals(1, code);
        String text = buf.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("Spec 文件不存在"));
        Files.deleteIfExists(dir);
    }
}
