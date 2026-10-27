package io.github.describeadmin.codegen;

import io.github.describeadmin.codegen.parser.SpecException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate25Test {

    @TempDir
    Path tmp;

    private static String run(String[] args) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        int code = CodegenCli.run(args, ps);
        assertEquals(0, code);
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void existingFilesAreSkippedUnlessForced() throws Exception {
        String[] fieldVariants = {
                "fields:\n  - name: title\n    type: string\n    comment: 标题\n",
                "fields:\n  - name: title\n    type: String\n    comment: 标题\n    length: 100\n",
                "fields:\n  - name: title\n    type: string\n    length: 100\n    required: true\n    comment: 标题\n"
        };
        String head = "package: com.example.demo\n"
                + "basePackage: com.example.demo\n"
                + "module: product\n"
                + "entity: Product\n"
                + "table: product\n"
                + "comment: 商品\n"
                + "apiPrefix: /product\n";

        Path spec = tmp.resolve("spec.yaml");
        Path outDir = null;
        String first = null;
        int n = 0;
        for (String fv : fieldVariants) {
            Files.writeString(spec, head + fv, StandardCharsets.UTF_8);
            Path candidate = tmp.resolve("out" + (n++));
            try {
                first = run(new String[]{spec.toString(), "--out", candidate.toString()});
                outDir = candidate;
                break;
            } catch (SpecException ignored) {
                // try next variant
            }
        }
        assertTrue(outDir != null, "no spec variant could be loaded");

        // First run: nothing exists, so everything must be generated, not skipped.
        assertFalse(first.contains("跳过  "), first);
        assertTrue(first.contains("生成  "), first);

        List<Path> generated;
        try (Stream<Path> w = Files.walk(outDir)) {
            generated = w.filter(Files::isRegularFile).collect(Collectors.toList());
        }
        assertFalse(generated.isEmpty());
        for (Path p : generated) {
            Files.writeString(p, "CUSTOM", StandardCharsets.UTF_8);
        }

        // Second run without --force: existing files are preserved.
        String second = run(new String[]{spec.toString(), "--out", outDir.toString()});
        assertTrue(second.contains("跳过  "), second);
        assertFalse(second.contains("覆盖  "), second);
        for (Path p : generated) {
            assertEquals("CUSTOM", Files.readString(p, StandardCharsets.UTF_8));
        }

        // With --force: they are overwritten.
        String third = run(new String[]{spec.toString(), "--out", outDir.toString(), "--force"});
        assertTrue(third.contains("覆盖  "), third);
        for (Path p : generated) {
            assertFalse("CUSTOM".equals(Files.readString(p, StandardCharsets.UTF_8)));
        }
    }
}
