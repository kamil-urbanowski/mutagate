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

class CodegenCliMutaGate44Test {

    @TempDir
    Path tmp;

    @Test
    void secondRunWithoutForceSkipsFilesAndPrintsHint() throws Exception {
        Path spec = tmp.resolve("notice.yaml");
        Files.writeString(spec, """
                basePackage: com.example.app
                module: notice
                entity: Notice
                table: biz_notice
                comment: 通知公告
                apiPrefix: /api/notice
                fields:
                  - name: title
                    type: String
                    comment: 标题
                """, StandardCharsets.UTF_8);

        Path outDir = tmp.resolve("out");

        ByteArrayOutputStream firstBuf = new ByteArrayOutputStream();
        PrintStream first = new PrintStream(firstBuf, true, StandardCharsets.UTF_8);
        int code1 = CodegenCli.run(new String[] {spec.toString(), "--out", outDir.toString()}, first);
        assertEquals(0, code1);

        ByteArrayOutputStream secondBuf = new ByteArrayOutputStream();
        PrintStream second = new PrintStream(secondBuf, true, StandardCharsets.UTF_8);
        int code2 = CodegenCli.run(new String[] {spec.toString(), "--out", outDir.toString()}, second);
        assertEquals(0, code2);

        String output = secondBuf.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("跳过"), output);
        assertTrue(output.contains("提示：Service / Controller 常含手工添加的业务逻辑，默认不覆盖是有意为之。"), output);
    }
}
