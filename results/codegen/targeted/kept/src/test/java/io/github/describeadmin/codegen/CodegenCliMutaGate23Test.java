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

class CodegenCliMutaGate23Test {

    @TempDir
    Path tmp;

    private String runCli(String... args) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        int code = CodegenCli.run(args, ps);
        ps.flush();
        assertEquals(0, code);
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void layoutLineIsPrintedOnlyForNonDefaultLayout() throws Exception {
        Path spec = tmp.resolve("spec.yaml");
        Files.writeString(spec, """
                basePackage: com.example.demo
                module: product
                entity: Product
                table: demo_product
                comment: 商品
                apiPrefix: /api/product
                fields:
                  - name: name
                    type: string
                    comment: 名称
                """, StandardCharsets.UTF_8);
        Path outDir = tmp.resolve("out");

        String flat = runCli(spec.toString(), "--out", outDir.toString(),
                "--layout", "flat", "--dry-run");
        assertTrue(flat.contains("布局: flat（来自 "), flat);

        String nested = runCli(spec.toString(), "--out", outDir.toString(),
                "--layout", "nested", "--dry-run");
        assertFalse(nested.contains("布局:"), nested);
    }
}
