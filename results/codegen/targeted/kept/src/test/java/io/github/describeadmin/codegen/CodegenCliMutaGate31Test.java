package io.github.describeadmin.codegen;

import io.github.describeadmin.codegen.parser.SpecException;
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

class CodegenCliMutaGate31Test {

    private static final String HINT = "提示：Service / Controller 常含手工添加的业务逻辑";

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
                + "    comment: 名称\n";
    }

    @Test
    void hintPrintedOnlyWhenFilesSkippedAndNotDryRun() throws Exception {
        String[] pkgKeys = {"basePackage", "package"};
        String[] types = {"string", "String", "STRING", "varchar"};

        SpecException last = null;
        int attempt = 0;
        for (String pk : pkgKeys) {
            for (String type : types) {
                attempt++;
                Path dir = tmp.resolve("a" + attempt);
                Files.createDirectories(dir);
                Path spec = dir.resolve("spec.yaml");
                Files.writeString(spec, spec(pk, type), StandardCharsets.UTF_8);
                Path outDir = dir.resolve("out");

                String first;
                try {
                    first = runCli(spec.toString(), "--out", outDir.toString(), "--layout", "nested");
                } catch (SpecException e) {
                    last = e;
                    continue;
                }

                // First run: nothing existed, nothing skipped -> no hint
                assertFalse(first.contains(HINT));

                // Second run: everything exists and is skipped, not dry-run -> hint
                String second = runCli(spec.toString(), "--out", outDir.toString(), "--layout", "nested");
                assertTrue(second.contains(HINT));

                // Third run: skipped but dry-run -> no hint
                String third = runCli(spec.toString(), "--out", outDir.toString(),
                        "--layout", "nested", "--dry-run");
                assertFalse(third.contains(HINT));
                return;
            }
        }
        throw last;
    }
}
