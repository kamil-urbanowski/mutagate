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

class CodegenCliMutaGate30Test {

    private static final String HINT = "提示：Service / Controller";

    private static String runCli(String... args) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        int code = CodegenCli.run(args, ps);
        ps.flush();
        assertEquals(0, code);
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void hintPrintedOnlyWhenFilesSkippedAndNotDryRun(@TempDir Path tmp) throws Exception {
        Path spec = tmp.resolve("spec.yaml");
        Files.writeString(spec, """
                basePackage: com.example.demo
                module: product
                entity: Product
                table: product
                comment: 商品
                apiPrefix: /product
                fields:
                  - name: name
                    type: string
                    comment: 名称
                """, StandardCharsets.UTF_8);
        Path outDir = tmp.resolve("out");

        // 1) 首次生成：没有跳过，不应出现提示
        String first = runCli(spec.toString(), "--out", outDir.toString());
        assertFalse(first.contains(HINT), first);

        // 2) 再次生成：全部已存在被跳过，且非 dry-run，应出现提示
        String second = runCli(spec.toString(), "--out", outDir.toString());
        assertTrue(second.contains(HINT), second);

        // 3) dry-run 且有跳过：不应出现提示
        String third = runCli(spec.toString(), "--out", outDir.toString(), "--dry-run");
        assertFalse(third.contains(HINT), third);
    }
}
