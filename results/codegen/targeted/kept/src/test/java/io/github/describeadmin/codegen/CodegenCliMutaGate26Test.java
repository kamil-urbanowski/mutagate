package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate26Test {

    @TempDir
    Path tmp;

    @Test
    void dryRunPrintsPlanAndWritesNothing() throws Exception {
        Path spec = tmp.resolve("spec.yaml");
        Files.writeString(spec, """
                basePackage: com.example.demo
                module: product
                entity: Product
                table: demo_product
                comment: 商品
                apiPrefix: /demo/product
                fields:
                  - name: productName
                    type: string
                    comment: 商品名称
                """, StandardCharsets.UTF_8);

        Path outDir = tmp.resolve("out");
        Files.createDirectories(outDir);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(
                new String[] {spec.toString(), "--out", outDir.toString(), "--dry-run"}, out);
        out.flush();
        String text = buf.toString(StandardCharsets.UTF_8);

        assertEquals(0, code, text);
        assertTrue(text.contains("将写"), text);
        assertFalse(text.contains("生成  "), text);
        assertTrue(text.contains("完成：将写入"), text);

        try (var children = Files.list(outDir)) {
            assertEquals(0, children.count(), "dry-run must not create any files");
        }
        assertFalse(Files.exists(outDir.resolve("src")));
        assertFalse(Files.exists(outDir.resolve("frontend")));
    }
}
