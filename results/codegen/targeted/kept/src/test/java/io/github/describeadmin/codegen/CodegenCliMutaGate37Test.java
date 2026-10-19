package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertTrue;

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

class CodegenCliMutaGate37Test {

    @TempDir
    Path tmp;

    @Test
    void printsTableAndFieldSummaryLine() throws Exception {
        List<Path> specs = new ArrayList<>();
        Path dir = Path.of("").toAbsolutePath();
        for (int i = 0; i < 4 && dir != null; i++, dir = dir.getParent()) {
            try (Stream<Path> walk = Files.walk(dir, 3)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> {
                            String n = p.getFileName().toString();
                            return n.endsWith(".yaml") || n.endsWith(".yml");
                        })
                        .filter(p -> p.getParent() != null
                                && "examples".equals(p.getParent().getFileName().toString()))
                        .filter(p -> !p.toString().contains("target")
                                && !p.toString().contains("node_modules"))
                        .sorted()
                        .forEach(specs::add);
            } catch (Exception ignored) {
                // keep looking
            }
            if (!specs.isEmpty()) {
                break;
            }
        }
        assertTrue(!specs.isEmpty(), "no example spec found");

        boolean checked = false;
        String lastOutput = "";
        for (Path spec : specs) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
            int code;
            try {
                code = CodegenCli.run(new String[] {
                        spec.toString(), "--out", tmp.resolve("out").toString(), "--dry-run"}, ps);
            } catch (Exception e) {
                continue;
            }
            ps.flush();
            String text = buf.toString(StandardCharsets.UTF_8);
            lastOutput = text;
            if (code == 0) {
                assertTrue(text.contains("表名: "), text);
                assertTrue(text.contains("字段: "), text);
                assertTrue(text.contains("接口前缀: "), text);
                checked = true;
                break;
            }
        }
        assertTrue(checked, "no example spec ran successfully: " + lastOutput);
    }
}
