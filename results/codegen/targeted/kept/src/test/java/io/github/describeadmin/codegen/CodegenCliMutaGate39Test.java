package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.describeadmin.codegen.parser.SpecException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate39Test {

    @TempDir
    Path tmp;

    private static List<Path> candidates() throws Exception {
        List<Path> result = new ArrayList<>();
        for (String dir : new String[] {"examples", "../examples", "../../examples"}) {
            Path p = Path.of(dir);
            if (Files.isDirectory(p)) {
                try (Stream<Path> s = Files.list(p)) {
                    s.filter(f -> {
                        String n = f.getFileName().toString();
                        return n.endsWith(".yaml") || n.endsWith(".yml");
                    }).sorted().forEach(result::add);
                }
            }
        }
        return result;
    }

    @Test
    void blankLineSeparatesHeaderFromFileList() throws Exception {
        List<Path> specs = candidates();
        assumeTrue(!specs.isEmpty(), "no example spec available");

        String output = null;
        for (Path spec : specs) {
            Path outDir = tmp.resolve("out-" + Math.abs(spec.toString().hashCode()));
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int code;
            try (PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8)) {
                code = CodegenCli.run(
                        new String[] {spec.toString(), "--out", outDir.toString(), "--dry-run"}, ps);
            } catch (SpecException e) {
                continue;
            }
            if (code == 0) {
                output = buf.toString(StandardCharsets.UTF_8);
                break;
            }
        }
        assumeTrue(output != null, "no example spec could be loaded");

        String[] lines = output.split("\\R", -1);
        int idx = -1;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("表名: ")) {
                idx = i;
                break;
            }
        }
        assertTrue(idx >= 0, "table line missing: " + output);
        int next = idx + 1;
        if (lines[next].startsWith("布局: ")) {
            next++;
        }
        assertEquals("", lines[next], "expected blank line after header, output: " + output);
        assertTrue(lines[next + 1].startsWith("  将写  "), "file list should follow: " + output);
    }
}
