package io.github.describeadmin.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate41Test {

    @TempDir
    Path tmp;

    @Test
    void dryRunPrintsPlannedFilesAndWritesNothing() throws Exception {
        Path spec = tmp.resolve("spec.yaml");
        Files.writeString(spec, """
                basePackage: com.example.demo
                module: product
                entity: Product
                table: demo_product
                comment: 商品
                apiPrefix: /product
                fields:
                  - name: name
                    type: string
                    comment: 名称
                """, StandardCharsets.UTF_8);

        Path outDir = tmp.resolve("out");

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int code;
        try (PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8)) {
            code = CodegenCli.run(new String[]{spec.toString(), "--out", outDir.toString(), "--dry-run"}, ps);
        }
        String output = buf.toString(StandardCharsets.UTF_8);

        assertEquals(0, code, output);
        assertTrue(output.contains("  将写  "), output);
        assertTrue(output.contains("schema-demo_product.sql"), output);
        assertTrue(output.contains("menu-demo_product.sql"), output);
        assertTrue(output.contains("完成：将写入 8 个文件，跳过 0 个"), output);
        assertFalse(Files.exists(outDir), "dry-run must not write files");
    }
}
