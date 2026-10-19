package io.github.describeadmin.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate43Test {

    @TempDir
    Path tmp;

    @Test
    void blankLineSeparatesFileListFromSummary() throws Exception {
        String yaml = String.join("\n",
                "package: com.example.demo",
                "basePackage: com.example.demo",
                "module: notice",
                "entity: Notice",
                "table: demo_notice",
                "comment: 公告",
                "apiPrefix: /demo/notice",
                "fields:",
                "  - name: title",
                "    type: string",
                "    comment: 标题",
                "");
        Path spec = tmp.resolve("spec.yaml");
        Files.writeString(spec, yaml, StandardCharsets.UTF_8);
        Path outDir = tmp.resolve("out");

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);

        int code = CodegenCli.run(new String[] {
                spec.toString(), "--out", outDir.toString(), "--dry-run"}, ps);
        ps.flush();

        assertEquals(0, code);

        List<String> lines = buf.toString(StandardCharsets.UTF_8).lines().toList();
        int summary = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).startsWith("完成：")) {
                summary = i;
                break;
            }
        }
        assertTrue(summary > 0, "summary line must be present");
        assertEquals("", lines.get(summary - 1));
        assertTrue(!lines.get(summary - 2).isEmpty(), "file list line precedes the blank line");
    }
}
