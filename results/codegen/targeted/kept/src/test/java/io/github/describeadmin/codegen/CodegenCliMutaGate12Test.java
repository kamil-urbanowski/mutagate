package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate12Test {

    @Test
    void missingSpecWithFlagReturnsOneWithoutOverrunningArgs(@TempDir Path tmp) throws Exception {
        Path missing = tmp.resolve("does-not-exist.yaml");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {missing.toString(), "--dry-run"}, out);

        out.flush();
        String text = buf.toString(StandardCharsets.UTF_8);
        assertEquals(1, code);
        assertTrue(text.contains(missing.toAbsolutePath().toString()), text);
    }
}
