package io.github.describeadmin.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class CodegenCliMutaGate39Test {

    @TempDir
    Path tmp;

    private List<Path> candidates() throws Exception {
        List<Path> result = new ArrayList<>();

        // Prefer shipped examples if they can be found near the working directory.
        Path dir = Path.of("").toAbsolutePath();
        for (int i = 0; i < 5 && dir != null; i++, dir = dir.getParent()) {
            Path ex = dir.resolve("examples");
            if (Files.isDirectory(ex)) {
                try (Stream<Path> s = Files.list(ex)) {
                    s.filter(p -> p.toString().endsWith(".yaml") || p.toString().endsWith(".yml"))
                            .sorted()
                            .forEach(result::add);
                }
            }
        }

        // Fallback: a hand-written spec.
        Path guessed = tmp.resolve("guessed-spec.yaml");
        Files.writeString(guessed, """
                basePackage: com.example.demo
                module: user
                entity: User
                table: sys_user
                comment: 用户
                apiPrefix: /system/user
                fields:
                  - name: username
                    type: string
                    comment: 用户名
                """, StandardCharsets.UTF_8);
        result.add(guessed);
        return result;
    }

    @Test
    void blankLineSeparatesHeaderFromFileList() throws Exception {
        List<String> failures = new ArrayList<>();
        for (Path spec : candidates()) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);
            Path outDir = Files.createTempDirectory(tmp, "out");
            int code;
            try {
                code = CodegenCli.run(new String[] {
                        spec.toString(), "--out", outDir.toString(), "--dry-run"}, out);
            } catch (Exception e) {
                failures.add(spec + ": " + e);
                continue;
            }
            if (code != 0) {
                failures.add(spec + ": exit " + code);
                continue;
            }
            out.flush();
            String text = buf.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
            String[] lines = text.split("\n", -1);

            int idx = -1;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].startsWith("表名: ")) {
                    idx = i;
                    break;
                }
            }
            if (idx < 0) {
                fail("header line not found in output:\n" + text);
            }
            int next = idx + 1;
            if (lines[next].startsWith("布局: ")) {
                next++;
            }
            assertEquals("", lines[next], "expected blank line after header, output:\n" + text);
            assertEquals(true, lines[next + 1].startsWith("  将写  "),
                    "expected first planned file after blank line, output:\n" + text);
            return;
        }
        fail("no spec could be loaded: " + failures);
    }
}
