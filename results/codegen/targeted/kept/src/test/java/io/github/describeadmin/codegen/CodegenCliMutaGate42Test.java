package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate42Test {

    @TempDir
    Path tmp;

    @Test
    void reportsEachGeneratedFileOnStdout() throws Exception {
        Path spec = tmp.resolve("product.yaml");
        Files.writeString(spec, """
                basePackage: com.example.shop
                module: product
                entity: Product
                table: product
                comment: 商品
                fields:
                  - name: name
                    type: string
                    comment: 名称
                """, StandardCharsets.UTF_8);

        Path outDir = tmp.resolve("out");
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {
                spec.toString(), "--out", outDir.toString(), "--layout", "nested"}, ps);
        ps.flush();
        String text = buf.toString(StandardCharsets.UTF_8);

        assertEquals(0, code, text);
        assertTrue(Files.exists(outDir.resolve("src/main/resources/db/schema-product.sql")), text);
        assertTrue(text.contains("  生成  src/main/resources/db/schema-product.sql"), text);
        assertTrue(text.contains("  生成  src/main/resources/db/menu-product.sql"), text);
    }
}
