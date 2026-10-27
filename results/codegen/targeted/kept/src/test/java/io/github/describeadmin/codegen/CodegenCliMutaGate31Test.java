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

class CodegenCliMutaGate31Test {

    private static final String HINT = "提示：Service / Controller";

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
    void hintPrintedOnlyWhenFilesSkippedAndNotDryRun() throws Exception {
        Path spec = tmp.resolve("spec.yaml");
        Files.writeString(spec, """
                basePackage: com.example.demo
                module: product
                entity: Product
                table: demo_product
                comment: 商品
                apiPrefix: /demo/product
                fields:
                  - name: name
                    type: string
                    comment: 名称
                """, StandardCharsets.UTF_8);
        Path outDir = tmp.resolve("out");

        // First run: nothing exists yet, nothing skipped -> no hint
        String first = runCli(spec.toString(), "--out", outDir.toString());
        assertFalse(first.contains(HINT), first);

        // Second run: everything exists and is skipped, not dry-run -> hint
        String second = runCli(spec.toString(), "--out", outDir.toString());
        assertTrue(second.contains(HINT), second);

        // Dry run with skipped files -> no hint
        String dry = runCli(spec.toString(), "--out", outDir.toString(), "--dry-run");
        assertFalse(dry.contains(HINT), dry);
    }
}
