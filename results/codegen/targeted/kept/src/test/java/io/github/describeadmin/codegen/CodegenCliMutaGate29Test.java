package io.github.describeadmin.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class CodegenCliMutaGate29Test {

    @TempDir
    Path tmp;

    private static List<Path> candidateSpecs() throws Exception {
        List<Path> result = new ArrayList<>();
        String[] roots = {"examples", "../examples", "../../examples", "src/test/resources", "src/main/resources"};
        for (String r : roots) {
            Path dir = Path.of(r);
            if (!Files.isDirectory(dir)) {
                continue;
            }
            try (Stream<Path> s = Files.walk(dir, 4)) {
                s.filter(Files::isRegularFile)
                        .filter(p -> {
                            String n = p.getFileName().toString();
                            return n.endsWith(".yaml") || n.endsWith(".yml");
                        })
                        .sorted()
                        .forEach(result::add);
            }
        }
        return result;
    }

    @Test
    void dryRunSummaryUsesWillWriteWording() throws Exception {
        List<Path> specs = candidateSpecs();
        Throwable last = null;
        int n = 0;
        for (Path specFile : specs) {
            Path outDir = Files.createTempDirectory(tmp, "out");
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
            int code;
            try {
                code = CodegenCli.run(new String[]{
                        specFile.toString(), "--out", outDir.toString(), "--dry-run"}, ps);
            } catch (RuntimeException e) {
                last = e;
                continue;
            }
            String text = buf.toString(StandardCharsets.UTF_8);
            assertEquals(0, code, text);
            assertTrue(text.contains("完成：将写入 8 个文件，跳过 0 个"), text);
            assertFalse(text.contains("完成：写入"), text);
            // dry-run must not write anything
            try (Stream<Path> s = Files.walk(outDir)) {
                assertEquals(0, s.filter(Files::isRegularFile).count());
            }
            n++;
            break;
        }
        if (n == 0) {
            fail("no loadable example spec found among " + specs + ": " + last);
        }
    }
}
