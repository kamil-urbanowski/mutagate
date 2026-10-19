package io.github.describeadmin.codegen;

import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate38Test {

    @TempDir
    Path tmp;

    private static List<String> candidates() {
        List<String> result = new ArrayList<>();
        String[] pkgKeys = {"basePackage", "package"};
        String[] types = {"string", "String", "varchar", "text"};
        String[] extras = {
                "    comment: 名称\n",
                "",
                "    label: 名称\n    comment: 名称\n"
        };
        for (String pk : pkgKeys) {
            for (String type : types) {
                for (String extra : extras) {
                    result.add(pk + ": com.example.demo\n"
                            + "module: book\n"
                            + "entity: Book\n"
                            + "table: demo_book\n"
                            + "comment: 图书\n"
                            + "apiPrefix: /demo/book\n"
                            + "fields:\n"
                            + "  - name: title\n"
                            + "    type: " + type + "\n"
                            + extra);
                }
            }
        }
        return result;
    }

    @Test
    void nonDefaultLayoutIsAnnouncedWithOrigin() throws Exception {
        Path spec = tmp.resolve("spec.yaml");
        boolean found = false;
        Exception last = null;
        for (String yaml : candidates()) {
            Files.writeString(spec, yaml, StandardCharsets.UTF_8);
            try {
                assertNotNull(SpecLoader.load(spec, "flat"));
                found = true;
                break;
            } catch (Exception e) {
                last = e;
            }
        }
        assertTrue(found, "no candidate spec could be loaded: " + last);

        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        Path outDir = tmp.resolve("out");
        int code = CodegenCli.run(new String[]{
                spec.toString(), "--out", outDir.toString(), "--layout", "flat", "--dry-run"}, ps);
        ps.flush();
        String output = buf.toString(StandardCharsets.UTF_8);

        assertEquals(0, code);
        assertTrue(output.contains("布局: flat（来自 "), output);
    }
}
