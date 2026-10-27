package io.github.describeadmin.codegen;

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

class CodegenCliMutaGate43Test {

    @TempDir
    Path tmp;

    private static Path findExampleSpec() throws Exception {
        Path dir = Path.of("").toAbsolutePath();
        for (int depth = 0; depth < 4 && dir != null; depth++, dir = dir.getParent()) {
            Path examples = dir.resolve("examples");
            if (Files.isDirectory(examples)) {
                try (Stream<Path> s = Files.walk(examples)) {
                    List<Path> found = s.filter(Files::isRegularFile)
                            .filter(p -> {
                                String n = p.getFileName().toString();
                                return n.endsWith(".yaml") || n.endsWith(".yml");
                            })
                            .sorted()
                            .toList();
                    if (!found.isEmpty()) {
                        return found.get(0);
                    }
                }
            }
        }
        return null;
    }

    @Test
    void blankLineSeparatesFileListFromSummary() throws Exception {
        Path spec = findExampleSpec();
        assertNotNull(spec, "no example spec found under examples/");

        Path outDir = tmp.resolve("out");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int code;
        try (PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8)) {
            code = CodegenCli.run(new String[] {
                    spec.toString(), "--out", outDir.toString(), "--dry-run"}, ps);
        }
        String output = buf.toString(StandardCharsets.UTF_8);
        String sep = System.lineSeparator();

        assertEquals(0, code, output);
        assertTrue(output.contains(sep + sep + "完成：将写入 "), output);
    }
}
