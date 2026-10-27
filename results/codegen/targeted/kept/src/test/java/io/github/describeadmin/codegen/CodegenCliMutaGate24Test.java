package io.github.describeadmin.codegen;

import io.github.describeadmin.codegen.parser.SpecException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class CodegenCliMutaGate24Test {

    @TempDir
    Path tmp;

    private static String yaml(String pkgKey, String type) {
        return pkgKey + ": com.example.demo\n"
                + "module: product\n"
                + "entity: Product\n"
                + "table: demo_product\n"
                + "comment: 商品\n"
                + "apiPrefix: /demo/product\n"
                + "fields:\n"
                + "  - name: name\n"
                + "    type: " + type + "\n"
                + "    comment: 名称\n";
    }

    @Test
    void existingFilesAreSkippedWithoutForce() throws Exception {
        List<String> candidates = new ArrayList<>();
        for (String pkgKey : new String[] {"package", "basePackage"}) {
            for (String type : new String[] {"string", "String"}) {
                candidates.add(yaml(pkgKey, type));
            }
        }

        boolean ran = false;
        int idx = 0;
        for (String content : candidates) {
            Path dir = tmp.resolve("case" + idx++);
            Files.createDirectories(dir);
            Path spec = dir.resolve("spec.yaml");
            Files.writeString(spec, content, StandardCharsets.UTF_8);
            Path outDir = dir.resolve("out");

            ByteArrayOutputStream first = new ByteArrayOutputStream();
            try {
                int code = CodegenCli.run(
                        new String[] {spec.toString(), "--out", outDir.toString()},
                        new PrintStream(first, true, StandardCharsets.UTF_8));
                assertEquals(0, code);
            } catch (SpecException e) {
                continue;
            }

            Path schema = outDir.resolve("src/main/resources/db/schema-demo_product.sql");
            assertTrue(Files.exists(schema), "first run should create the schema file");

            Files.writeString(schema, "SENTINEL", StandardCharsets.UTF_8);

            ByteArrayOutputStream second = new ByteArrayOutputStream();
            int code2 = CodegenCli.run(
                    new String[] {spec.toString(), "--out", outDir.toString()},
                    new PrintStream(second, true, StandardCharsets.UTF_8));
            assertEquals(0, code2);

            assertEquals("SENTINEL", Files.readString(schema, StandardCharsets.UTF_8),
                    "existing file must not be overwritten without --force");
            String text = second.toString(StandardCharsets.UTF_8);
            assertTrue(text.contains("跳过"), text);
            assertTrue(text.contains("跳过 8 个"), text);

            ran = true;
            break;
        }

        if (!ran) {
            fail("no candidate spec could be loaded");
        }
    }
}
