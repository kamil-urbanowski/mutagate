package io.github.describeadmin.codegen;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate19Test {

    @Test
    void emptyArgsReturnOneAndHelpReturnsZero() throws Exception {
        ByteArrayOutputStream buf1 = new ByteArrayOutputStream();
        PrintStream out1 = new PrintStream(buf1, true, StandardCharsets.UTF_8);
        int noArgs = CodegenCli.run(new String[0], out1);

        ByteArrayOutputStream buf2 = new ByteArrayOutputStream();
        PrintStream out2 = new PrintStream(buf2, true, StandardCharsets.UTF_8);
        int help = CodegenCli.run(new String[] {"--help"}, out2);

        ByteArrayOutputStream buf3 = new ByteArrayOutputStream();
        PrintStream out3 = new PrintStream(buf3, true, StandardCharsets.UTF_8);
        int shortHelp = CodegenCli.run(new String[] {"-h"}, out3);

        assertEquals(1, noArgs);
        assertEquals(0, help);
        assertEquals(0, shortHelp);
        assertTrue(buf1.toString(StandardCharsets.UTF_8).contains("codegen"));
        assertTrue(buf2.toString(StandardCharsets.UTF_8).contains("codegen"));
    }
}
