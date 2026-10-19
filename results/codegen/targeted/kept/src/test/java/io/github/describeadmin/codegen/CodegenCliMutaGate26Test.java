package io.github.describeadmin.codegen;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class CodegenCliMutaGate26Test {

    @TempDir
    Path temp;

    private static List<Path> exampleSpecs() throws Exception {
        List<Path> result = new ArrayList<>();
        Path base = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        for (int up = 0; up < 4 && base != null; up++, base = base.getParent()) {
            for (Path dir : new Path[]{base.resolve("examples"), base.resolve("codegen/examples")}) {
                if (Files.isDirectory(dir)) {
                    try (Stream<Path> s = Files.list(dir)) {
                        s.filter(p -> {
                            String n = p.getFileName().toString();
                            return n.endsWith(".yaml") || n.endsWith(".yml");
                        }).sorted().forEach(result::add);
                    }
                }
            }
        }
        return result;
    }

    @Test
    void dryRunDoesNotWriteAnyFile() throws Exception {
        List<Path> candidates = exampleSpecs();
        Path inline = temp.resolve("inline.yaml");
        Files.writeString(inline, String.join("\n",
                "basePackage: com.example.demo",
                "module: product",
                "entity: Product",
                "table: demo_product",
                "comment: 商品",
                "apiPrefix: /demo/product",
                "fields:",
                "  - name: name",
                "    type: string",
                "    comment: 名称",
                ""), StandardCharsets.UTF_8);
        candidates.add(inline);

        int idx = 0;
        for (Path spec : candidates) {
            Path outDir = temp.resolve("out" + idx++);
            Path feDir = outDir.resolve("fe");
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
            int code;
            try {
                code = CodegenCli.run(new String[]{
                        spec.toString(), "--out", outDir.toString(),
                        "--frontend-out", feDir.toString(), "--dry-run"}, ps);
            } catch (Exception e) {
                continue; // spec not loadable, try next candidate
            }
            String output = buf.toString(StandardCharsets.UTF_8);
            assertEquals(0, code, output);
            assertTrue(output.contains("将写  "), output);
            assertTrue(output.contains("将写入"), output);

            long fileCount = 0;
            if (Files.exists(outDir)) {
                try (Stream<Path> w = Files.walk(outDir)) {
                    fileCount = w.filter(Files::isRegularFile).count();
                }
            }
            assertEquals(0, fileCount, "dry-run must not write files: " + output);
            return;
        }
        fail("no loadable spec found");
    }
}
