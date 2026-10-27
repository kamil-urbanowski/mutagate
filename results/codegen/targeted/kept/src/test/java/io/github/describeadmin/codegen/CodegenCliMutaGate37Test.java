package io.github.describeadmin.codegen;

import io.github.describeadmin.codegen.parser.SpecException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class CodegenCliMutaGate37Test {

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
                + "    comment: 名称\n"
                + "    required: true\n";
    }

    @Test
    void printsTableLineInSummary() throws Exception {
        String[] pkgKeys = {"package", "basePackage", "base-package"};
        String[] types = {"string", "String", "varchar"};
        StringBuilder failures = new StringBuilder();

        for (String pkgKey : pkgKeys) {
            for (String type : types) {
                Path spec = tmp.resolve("spec-" + pkgKey + "-" + type + ".yaml");
                Files.writeString(spec, yaml(pkgKey, type), StandardCharsets.UTF_8);
                Path outDir = tmp.resolve("out-" + pkgKey + "-" + type);

                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
                int code;
                try {
                    code = CodegenCli.run(
                            new String[]{spec.toString(), "--out", outDir.toString(), "--dry-run"}, ps);
                } catch (SpecException e) {
                    failures.append(e.getMessage()).append('\n');
                    continue;
                }
                String text = buf.toString(StandardCharsets.UTF_8);
                assertTrue(code == 0, "unexpected exit code, output: " + text);
                assertTrue(text.contains("模块: 商品 (product)"), text);
                assertTrue(text.contains("表名: demo_product"), text);
                assertTrue(text.contains("字段: "), text);
                return;
            }
        }
        fail("No candidate spec could be loaded: " + failures);
    }
}
