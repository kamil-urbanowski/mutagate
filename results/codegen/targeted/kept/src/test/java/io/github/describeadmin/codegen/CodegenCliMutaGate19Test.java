package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CodegenCliMutaGate19Test {

    @Test
    void emptyArgsReturnsOneAndHelpFlagsReturnZero() throws IOException {
        ByteArrayOutputStream buf1 = new ByteArrayOutputStream();
        PrintStream out1 = new PrintStream(buf1, true, StandardCharsets.UTF_8);
        assertEquals(1, CodegenCli.run(new String[0], out1));

        ByteArrayOutputStream buf2 = new ByteArrayOutputStream();
        PrintStream out2 = new PrintStream(buf2, true, StandardCharsets.UTF_8);
        assertEquals(0, CodegenCli.run(new String[] {"--help"}, out2));

        ByteArrayOutputStream buf3 = new ByteArrayOutputStream();
        PrintStream out3 = new PrintStream(buf3, true, StandardCharsets.UTF_8);
        assertEquals(0, CodegenCli.run(new String[] {"-h"}, out3));
    }
}
