package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate31Test {

    @TempDir
    Path tmp;

    private static int invoke(String[] args, ByteArrayOutputStream buf) throws IOException {
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        int code = CodegenCli.run(args, ps);
        ps.flush();
        return code;
    }

    @Test
    void earlyExitPathsReturnExpectedCodesAndMessages() throws IOException {
        // no args: usage, exit code 1
        ByteArrayOutputStream b1 = new ByteArrayOutputStream();
        assertEquals(1, invoke(new String[0], b1));
        String o1 = b1.toString(StandardCharsets.UTF_8);
        assertTrue(o1.contains("用法:"));
        assertTrue(o1.contains("--dry-run"));

        // -h and --help: usage, exit code 0
        ByteArrayOutputStream b2 = new ByteArrayOutputStream();
        assertEquals(0, invoke(new String[] {"-h"}, b2));
        assertTrue(b2.toString(StandardCharsets.UTF_8).contains("用法:"));

        ByteArrayOutputStream b3 = new ByteArrayOutputStream();
        assertEquals(0, invoke(new String[] {"--help", "ignored"}, b3));
        assertTrue(b3.toString(StandardCharsets.UTF_8).contains("--frontend-out"));

        // unknown option: error line followed by usage, exit code 1
        ByteArrayOutputStream b4 = new ByteArrayOutputStream();
        assertEquals(1, invoke(new String[] {"some.yaml", "--bogus"}, b4));
        String o4 = b4.toString(StandardCharsets.UTF_8);
        assertTrue(o4.startsWith("✗ 未知参数: --bogus"));
        assertTrue(o4.contains("用法:"));

        // missing spec file: error with absolute path, exit code 1, nothing written
        Path missing = tmp.resolve("nope.yaml");
        Path outDir = tmp.resolve("out");
        Files.createDirectories(outDir);
        ByteArrayOutputStream b5 = new ByteArrayOutputStream();
        int code = invoke(new String[] {missing.toString(), "--out", outDir.toString(), "--force"}, b5);
        assertEquals(1, code);
        String o5 = b5.toString(StandardCharsets.UTF_8);
        assertTrue(o5.contains("✗ Spec 文件不存在: " + missing.toAbsolutePath()));
        assertTrue(!o5.contains("完成"));
        try (Stream<Path> s = Files.list(outDir)) {
            assertEquals(0, s.count());
        }
    }
}
