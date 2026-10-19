package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate28Test {

    @TempDir
    Path tmp;

    private static Path findExampleSpec() throws Exception {
        Path dir = Path.of("").toAbsolutePath();
        for (int i = 0; i < 4 && dir != null; i++, dir = dir.getParent()) {
            Path examples = dir.resolve("examples");
            if (Files.isDirectory(examples)) {
                try (Stream<Path> s = Files.walk(examples)) {
                    List<Path> found = s.filter(Files::isRegularFile)
                            .filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"))
                            .sorted()
                            .collect(Collectors.toList());
                    if (!found.isEmpty()) {
                        return found.get(0);
                    }
                }
            }
        }
        return null;
    }

    private static String runCli(String... args) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8)) {
            int code = CodegenCli.run(args, ps);
            assertEquals(0, code);
        }
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void newFilesReportedAsGeneratedAndForcedOnesAsOverwritten() throws Exception {
        Path spec = findExampleSpec();
        assumeTrue(spec != null, "no example spec found");

        Path outDir = tmp.resolve("out");
        String first = runCli(spec.toString(), "--out", outDir.toString());
        assertTrue(first.contains("  生成  "), first);
        assertFalse(first.contains("  覆盖  "), first);

        String second = runCli(spec.toString(), "--out", outDir.toString(), "--force");
        assertTrue(second.contains("  覆盖  "), second);
        assertFalse(second.contains("  生成  "), second);
    }
}
