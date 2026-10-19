package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class CodegenCliMutaGate42Test {

    @Test
    void reportsGeneratedFilesWhenWritingToFreshDirectory() throws Exception {
        String[] packageKeys = {"basePackage", "package"};
        String[] types = {"string", "String"};
        String lastFailure = "none";

        for (String packageKey : packageKeys) {
            for (String type : types) {
                Path dir = Files.createTempDirectory("codegen-cli-test");
                Path spec = dir.resolve("spec.yaml");
                String yaml = packageKey + ": com.example.demo\n"
                        + "module: book\n"
                        + "entity: Book\n"
                        + "table: demo_book\n"
                        + "comment: Book\n"
                        + "apiPrefix: /demo/book\n"
                        + "fields:\n"
                        + "  - name: title\n"
                        + "    type: " + type + "\n"
                        + "    comment: Title\n";
                Files.writeString(spec, yaml, StandardCharsets.UTF_8);
                Path outDir = dir.resolve("out");

                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                PrintStream out = new PrintStream(buf, true, StandardCharsets.UTF_8);
                int code;
                try {
                    code = CodegenCli.run(new String[]{
                            spec.toString(), "--out", outDir.toString()}, out);
                } catch (RuntimeException e) {
                    lastFailure = String.valueOf(e);
                    continue;
                }
                out.flush();
                String text = buf.toString(StandardCharsets.UTF_8);
                if (code != 0) {
                    lastFailure = "exit code " + code + ": " + text;
                    continue;
                }
                String generated = "  \u751f\u6210  ";
                assertTrue(text.contains(generated + "src/main/resources/db/schema-demo_book.sql"),
                        text);
                assertTrue(text.contains(generated + "src/main/resources/db/menu-demo_book.sql"),
                        text);
                return;
            }
        }
        fail("No candidate spec could be loaded: " + lastFailure);
    }
}
