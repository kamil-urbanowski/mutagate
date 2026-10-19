package io.github.describeadmin.codegen;

import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate21Test {

    private static Path findLoadableSpec() throws Exception {
        for (String dir : List.of("examples", "../examples", "../../examples")) {
            Path d = Path.of(dir);
            if (!Files.isDirectory(d)) {
                continue;
            }
            List<Path> candidates;
            try (Stream<Path> walk = Files.walk(d, 3)) {
                candidates = walk.filter(Files::isRegularFile)
                        .filter(p -> {
                            String n = p.getFileName().toString();
                            return n.endsWith(".yaml") || n.endsWith(".yml");
                        })
                        .sorted()
                        .toList();
            }
            for (Path p : candidates) {
                try {
                    SpecLoader.load(p);
                    return p;
                } catch (Exception ignored) {
                    // try next candidate
                }
            }
        }
        return null;
    }

    @Test
    void defaultFrontendOutIsUnderOutDir(@TempDir Path tmp) throws Exception {
        Path spec = findLoadableSpec();
        assertNotNull(spec, "no loadable example spec found");

        Path outDir = tmp.resolve("out");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(
                new String[]{spec.toString(), "--out", outDir.toString(), "--dry-run"}, ps);
        ps.flush();
        String text = buf.toString(StandardCharsets.UTF_8);

        assertEquals(0, code);
        assertTrue(text.contains("frontend/src/api/"), text);
        assertTrue(text.contains("frontend/src/views/"), text);
    }
}
