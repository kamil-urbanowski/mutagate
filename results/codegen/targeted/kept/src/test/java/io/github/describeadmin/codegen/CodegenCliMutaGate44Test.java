package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate44Test {

    private static final String HINT = "提示：Service / Controller 常含手工添加的业务逻辑，默认不覆盖是有意为之。";

    private static final List<String> CANDIDATES = List.of(
            """
            basePackage: com.example.demo
            module: product
            entity: Product
            table: t_product
            comment: 商品
            apiPrefix: /api/product
            fields:
              - name: name
                type: string
                comment: 名称
            """,
            """
            basePackage: com.example.demo
            module: product
            entity: Product
            table: t_product
            comment: 商品
            apiPrefix: /api/product
            fields:
              - name: name
                type: String
                comment: 名称
                required: true
                query: true
            """,
            """
            package: com.example.demo
            module: product
            entity: Product
            table: t_product
            comment: 商品
            apiPrefix: /api/product
            fields:
              - name: name
                type: string
                comment: 名称
            """,
            """
            basePackage: com.example.demo
            module: product
            entity: Product
            table: t_product
            comment: 商品
            fields:
              - name: name
                type: string
                label: 名称
            """);

    @Test
    void secondRunWithoutForcePrintsKeepHint(@TempDir Path tmp) throws Exception {
        int idx = 0;
        for (String yaml : CANDIDATES) {
            Path dir = tmp.resolve("c" + idx++);
            Files.createDirectories(dir);
            Path spec = dir.resolve("spec.yaml");
            Files.writeString(spec, yaml, StandardCharsets.UTF_8);
            Path outDir = dir.resolve("out");
            String[] args = {spec.toString(), "--out", outDir.toString()};

            ByteArrayOutputStream first = new ByteArrayOutputStream();
            try (PrintStream ps = new PrintStream(first, true, StandardCharsets.UTF_8)) {
                try {
                    if (CodegenCli.run(args, ps) != 0) {
                        continue;
                    }
                } catch (RuntimeException e) {
                    continue;
                }
            }
            String firstText = first.toString(StandardCharsets.UTF_8);
            assertFalse(firstText.contains(HINT));

            ByteArrayOutputStream second = new ByteArrayOutputStream();
            int code;
            try (PrintStream ps = new PrintStream(second, true, StandardCharsets.UTF_8)) {
                code = CodegenCli.run(args, ps);
            }
            String secondText = second.toString(StandardCharsets.UTF_8);
            assertEquals(0, code);
            assertTrue(secondText.contains("跳过  "), secondText);
            assertTrue(secondText.contains(HINT), secondText);
            return;
        }
        throw new AssertionError("no candidate spec could be loaded");
    }
}
