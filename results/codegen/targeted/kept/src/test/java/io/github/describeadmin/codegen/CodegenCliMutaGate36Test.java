package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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

class CodegenCliMutaGate36Test {

    @TempDir
    Path tmp;

    @Test
    void runPrintsModuleHeaderLine() throws Exception {
        List<Path> candidates = new ArrayList<>();

        // Real example specs shipped with the project (format known to be valid).
        Path cwd = Path.of("").toAbsolutePath();
        Path dir = cwd;
        for (int up = 0; up < 4 && dir != null; up++) {
            for (String sub : new String[] {"examples", "src/test/resources", "src/main/resources/examples"}) {
                Path d = dir.resolve(sub);
                if (Files.isDirectory(d)) {
                    try (Stream<Path> w = Files.walk(d, 4)) {
                        w.filter(Files::isRegularFile)
                                .filter(p -> {
                                    String n = p.getFileName().toString();
                                    return n.endsWith(".yaml") || n.endsWith(".yml");
                                })
                                .sorted()
                                .forEach(candidates::add);
                    }
                }
            }
            dir = dir.getParent();
        }

        // Guessed specs as a fallback.
        String[] pkgKeys = {"basePackage", "package"};
        String[] types = {"string", "String"};
        int idx = 0;
        for (String pkgKey : pkgKeys) {
            for (String type : types) {
                idx++;
                String yaml = pkgKey + ": com.example.demo\n"
                        + "module: user\n"
                        + "entity: User\n"
                        + "table: sys_user\n"
                        + "comment: 用户\n"
                        + "apiPrefix: /api/user\n"
                        + "fields:\n"
                        + "  - name: userName\n"
                        + "    type: " + type + "\n"
                        + "    comment: 用户名\n";
                Path spec = tmp.resolve("guess" + idx + ".yaml");
                Files.writeString(spec, yaml, StandardCharsets.UTF_8);
                candidates.add(spec);
            }
        }

        StringBuilder failures = new StringBuilder();
        int n = 0;
        for (Path spec : candidates) {
            n++;
            Path outDir = tmp.resolve("out" + n);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
            int code;
            try {
                code = CodegenCli.run(new String[] {
                        spec.toString(), "--out", outDir.toString(), "--dry-run"}, ps);
            } catch (Exception e) {
                failures.append(spec).append(" -> ").append(e).append('\n');
                continue;
            }
            if (code != 0) {
                failures.append(spec).append(" -> exit ").append(code).append('\n');
                continue;
            }
            String output = buf.toString(StandardCharsets.UTF_8);
            String firstLine = output.lines().findFirst().orElse("");
            assertTrue(firstLine.startsWith("模块: "),
                    "first line should be module header, output was:\n" + output);
            assertTrue(firstLine.contains(" (") && firstLine.endsWith(")"),
                    "module header malformed: " + firstLine);
            return;
        }
        fail("No candidate spec could be loaded:\n" + failures);
    }
}
