package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.describeadmin.codegen.parser.SpecException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate21Test {

    private static String spec(String pkgKey, String type) {
        return pkgKey + ": com.example.demo\n"
                + "module: product\n"
                + "entity: Product\n"
                + "table: demo_product\n"
                + "comment: 商品\n"
                + "apiPrefix: /demo/product\n"
                + "fields:\n"
                + "  - name: name\n"
                + "    type: " + type + "\n"
                + "    comment: 名称\n"
                + "    required: true\n";
    }

    @Test
    void frontendOutputDefaultsToFrontendSubdirectoryOfOut(@TempDir Path tmp) throws Exception {
        String[] pkgKeys = {"basePackage", "package", "packageName"};
        String[] types = {"string", "String", "varchar"};
        List<String> problems = new ArrayList<>();

        int attempt = 0;
        for (String pkgKey : pkgKeys) {
            for (String type : types) {
                attempt++;
                Path dir = tmp.resolve("attempt" + attempt);
                Files.createDirectories(dir);
                Path specFile = dir.resolve("product.yaml");
                Files.writeString(specFile, spec(pkgKey, type), StandardCharsets.UTF_8);

                Path outDir = dir.resolve("out");
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);

                int code;
                try {
                    code = CodegenCli.run(
                            new String[] {specFile.toString(), "--out", outDir.toString()}, out);
                } catch (SpecException e) {
                    problems.add(pkgKey + "/" + type + ": " + e.errors());
                    continue;
                }

                assertEquals(0, code);
                assertTrue(Files.exists(outDir.resolve("frontend/src/api/product.ts")));
                assertTrue(Files.exists(outDir.resolve("frontend/src/views/product/index.vue")));
                return;
            }
        }
        fail("No spec variant was accepted: " + problems);
    }
}
