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

    @TempDir
    Path tmp;

    private static String yaml(String pkgKey, String type, String apiPrefix) {
        return pkgKey + ": com.example.demo\n"
                + "module: user\n"
                + "entity: User\n"
                + "table: sys_user\n"
                + "comment: 用户\n"
                + "apiPrefix: " + apiPrefix + "\n"
                + "fields:\n"
                + "  - name: userName\n"
                + "    type: " + type + "\n"
                + "    comment: 用户名\n";
    }

    private Path findLoadableSpec() throws Exception {
        List<Path> candidates = new ArrayList<>();
        int n = 0;
        for (String pkgKey : new String[]{"basePackage", "package"}) {
            for (String type : new String[]{"string", "String", "varchar"}) {
                for (String api : new String[]{"/system/user", "/api/user"}) {
                    Path p = tmp.resolve("cand" + (n++) + ".yaml");
                    Files.writeString(p, yaml(pkgKey, type, api), StandardCharsets.UTF_8);
                    candidates.add(p);
                }
            }
        }
        Path examples = Path.of("examples");
        if (Files.isDirectory(examples)) {
            try (Stream<Path> st = Files.list(examples)) {
                st.filter(f -> f.toString().endsWith(".yaml") || f.toString().endsWith(".yml"))
                        .sorted()
                        .forEach(candidates::add);
            }
        }
        for (Path c : candidates) {
            try {
                SpecLoader.load(c);
                return c;
            } catch (Exception ignored) {
                // try next candidate
            }
        }
        return null;
    }

    @Test
    void dryRunWithForceMarksOnlyExistingFilesAsOverwrite() throws Exception {
        Path spec = findLoadableSpec();
        assertNotNull(spec, "no loadable spec found");

        Path outDir = tmp.resolve("out");
        Files.createDirectories(outDir);

        ModuleSpec s = SpecLoader.load(spec);
        Map<Path, String> plan = CodegenCli.plan(s, outDir, outDir.resolve("frontend"));
        Path existing = plan.keySet().iterator().next();
        Files.createDirectories(existing.getParent());
        Files.writeString(existing, "// hand-written", StandardCharsets.UTF_8);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[]{
                spec.toString(), "--out", outDir.toString(), "--dry-run", "--force"}, ps);
        ps.flush();
        String output = buf.toString(StandardCharsets.UTF_8);

        assertEquals(0, code, output);

        long overwriteMarks = output.lines().filter(l -> l.contains("(覆盖)")).count();
        assertEquals(1, overwriteMarks, output);

        String existingName = existing.getFileName().toString();
        assertTrue(output.lines().anyMatch(l ->
                l.startsWith("  将写  ") && l.contains(existingName) && l.endsWith("(覆盖)")), output);

        // dry-run must not touch the existing file
        assertEquals("// hand-written", Files.readString(existing, StandardCharsets.UTF_8));
    }
}
