package io.github.describeadmin.codegen;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate27Test {

    private static final String GUESSED_SPEC = """
            basePackage: com.example
            module: product
            entity: Product
            table: product
            comment: 商品
            apiPrefix: /api/product
            fields:
              - name: name
                type: string
                comment: 名称
            """;

    @TempDir
    Path tmp;

    private Path findLoadableSpec() throws Exception {
        List<Path> candidates = new ArrayList<>();
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null) {
            Path ex = dir.resolve("examples");
            if (Files.isDirectory(ex)) {
                try (Stream<Path> st = Files.list(ex)) {
                    st.filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"))
                            .sorted().forEach(candidates::add);
                }
            }
            dir = dir.getParent();
        }
        Path guessed = tmp.resolve("guessed.yaml");
        Files.writeString(guessed, GUESSED_SPEC, StandardCharsets.UTF_8);
        candidates.add(guessed);
        for (Path c : candidates) {
            try {
                SpecLoader.load(c);
                return c;
            } catch (Exception | LinkageError e) {
                // try next
            }
        }
        return null;
    }

    @Test
    void dryRunMarksOverwriteOnlyForExistingFiles() throws Exception {
        Path spec = findLoadableSpec();
        assertNotNull(spec, "no loadable spec found");

        Path outDir = tmp.resolve("out");
        Path frontOut = outDir.resolve("frontend");
        ModuleSpec s = SpecLoader.load(spec);
        Map<Path, String> planned = CodegenCli.plan(s, outDir, frontOut);

        Path existing = planned.keySet().iterator().next();
        Files.createDirectories(existing.getParent());
        Files.writeString(existing, "old", StandardCharsets.UTF_8);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(bos, true, StandardCharsets.UTF_8);
        int code = CodegenCli.run(new String[]{
                spec.toString(), "--out", outDir.toString(), "--dry-run", "--force"}, ps);
        ps.flush();
        assertEquals(0, code);

        String text = bos.toString(StandardCharsets.UTF_8);
        List<String> planLines = new ArrayList<>();
        for (String line : text.split("\\R")) {
            if (line.startsWith("  将写  ")) {
                planLines.add(line);
            }
        }
        assertEquals(planned.size(), planLines.size());

        List<String> overwrite = planLines.stream().filter(l -> l.endsWith("(覆盖)")).toList();
        assertEquals(1, overwrite.size());
        assertTrue(overwrite.get(0).contains(existing.getFileName().toString()));
        // existing file untouched in dry-run
        assertEquals("old", Files.readString(existing, StandardCharsets.UTF_8));
    }
}
