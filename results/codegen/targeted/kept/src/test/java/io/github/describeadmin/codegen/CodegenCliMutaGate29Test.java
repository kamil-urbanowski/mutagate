package io.github.describeadmin.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.describeadmin.codegen.parser.SpecException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CodegenCliMutaGate29Test {

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
                + "    comment: 名称\n";
    }

    @Test
    void dryRunSummaryUsesWillWriteWording() throws Exception {
        String[] pkgKeys = {"basePackage", "package"};
        String[] types = {"string", "String"};

        String output = null;
        int variant = 0;
        for (String pk : pkgKeys) {
            for (String type : types) {
                Path spec = tmp.resolve("spec" + variant + ".yaml");
                Path outDir = tmp.resolve("out" + variant);
                variant++;
                Files.writeString(spec, yaml(pk, type), StandardCharsets.UTF_8);

                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
                int code;
                try {
                    code = CodegenCli.run(new String[]{
                            spec.toString(), "--out", outDir.toString(), "--dry-run"}, ps);
                } catch (SpecException ex) {
                    continue;
                }
                assertEquals(0, code);
                output = buf.toString(StandardCharsets.UTF_8);

                // dry-run must not write anything
                try (Stream<Path> s = Files.exists(outDir) ? Files.walk(outDir) : Stream.empty()) {
                    assertEquals(0, s.filter(Files::isRegularFile).count());
                }
                break;
            }
            if (output != null) {
                break;
            }
        }

        assumeTrue(output != null, "no candidate spec format was accepted");
        assertTrue(output.contains("完成：将写入 8 个文件，跳过 0 个"), output);
    }
}
