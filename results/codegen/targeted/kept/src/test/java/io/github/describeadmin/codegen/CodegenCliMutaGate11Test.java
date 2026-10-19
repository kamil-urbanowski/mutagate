package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class CodegenCliMutaGate11Test {

    @Test
    void helpPrintsUsageAndReturnsZero() throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {"--help"}, out);
        out.flush();

        String text = buf.toString(StandardCharsets.UTF_8);
        assertEquals(0, code);
        assertTrue(text.contains("describeadmin codegen"), text);
        assertTrue(text.contains("--dry-run"), text);
        assertTrue(text.contains("menu-<table>.sql 必须执行"), text);
    }
}
