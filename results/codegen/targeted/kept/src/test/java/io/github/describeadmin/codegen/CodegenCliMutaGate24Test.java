package io.github.describeadmin.codegen;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate24Test {

    private static Path findLoadableSpec() {
        Path dir = Path.of("").toAbsolutePath();
        for (int up = 0; up < 6 && dir != null; up++, dir = dir.getParent()) {
            Path examples = dir.resolve("examples");
            if (!Files.isDirectory(examples)) {
                continue;
            }
            List<Path> candidates = new ArrayList<>();
            try (Stream<Path> walk = Files.walk(examples, 3)) {
                walk.filter(Files::isRegularFile)
                        .filter(p -> {
                            String n = p.getFileName().toString();
                            return n.endsWith(".yaml") || n.endsWith(".yml");
                        })
                        .sorted()
                        .forEach(candidates::add);
            } catch (Exception e) {
                continue;
            }
            for (Path c : candidates) {
                try {
                    SpecLoader.load(c, null);
                    return c;
                } catch (Exception ignored) {
                    // try next
                }
            }
        }
        return null;
    }

    @Test
    void existingFileIsSkippedWithoutForce() throws Exception {
        Path spec = findLoadableSpec();
        Assumptions.assumeTrue(spec != null, "no loadable example spec found");

        Path outDir = Files.createTempDirectory("codegen-cli-test");
        Path frontendOut = outDir.resolve("frontend");

        ModuleSpec s = SpecLoader.load(spec, null);
        Map<Path, String> plan = CodegenCli.plan(s, outDir, frontendOut);
        Path existing = plan.keySet().iterator().next();
        Files.createDirectories(existing.getParent());
        String sentinel = "SENTINEL-DO-NOT-OVERWRITE";
        Files.writeString(existing, sentinel, StandardCharsets.UTF_8);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(
                new String[] {spec.toString(), "--out", outDir.toString()}, out);
        String output = buf.toString(StandardCharsets.UTF_8);

        assertEquals(0, code);
        assertEquals(sentinel, Files.readString(existing, StandardCharsets.UTF_8));
        assertTrue(output.contains("跳过"), output);
        assertTrue(output.contains("跳过 1 个"), output);
        for (Path p : plan.keySet()) {
            assertTrue(Files.exists(p), "missing " + p);
        }
    }
}
