package io.github.describeadmin.codegen;

import io.github.describeadmin.codegen.parser.SpecException;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate36Test {

    @TempDir
    Path tmp;

    private static List<Path> findSpecs() throws IOException {
        List<Path> result = new ArrayList<>();
        for (String dir : new String[] {"examples", "../examples", "../../examples"}) {
            Path p = Path.of(dir);
            if (!Files.isDirectory(p)) {
                continue;
            }
            try (Stream<Path> s = Files.walk(p, 3)) {
                s.filter(Files::isRegularFile)
                        .filter(f -> {
                            String n = f.getFileName().toString();
                            return n.endsWith(".yaml") || n.endsWith(".yml");
                        })
                        .sorted()
                        .forEach(result::add);
            }
            if (!result.isEmpty()) {
                break;
            }
        }
        return result;
    }

    @Test
    void runPrintsModuleHeaderLine() throws IOException {
        List<Path> specs = findSpecs();
        Assumptions.assumeFalse(specs.isEmpty(), "no example spec available");

        String text = null;
        for (Path spec : specs) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);
            int code;
            try {
                code = CodegenCli.run(new String[] {
                        spec.toString(), "--out", tmp.resolve("out").toString(), "--dry-run"}, out);
            } catch (SpecException e) {
                continue;
            }
            if (code == 0) {
                text = buf.toString(StandardCharsets.UTF_8);
                break;
            }
        }
        Assumptions.assumeTrue(text != null, "no loadable example spec");

        String firstLine = text.lines().findFirst().orElse("");
        assertTrue(firstLine.startsWith("模块: "), "first line was: " + firstLine);
        assertTrue(firstLine.endsWith(")"), "first line was: " + firstLine);
        assertEquals(1, text.lines().filter(l -> l.startsWith("模块: ")).count());
    }
}
