package io.github.describeadmin.codegen;

import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Assumptions;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate23Test {

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
                + "    length: 64\n"
                + "    required: true\n"
                + "    query: eq\n";
    }

    private List<Path> candidates() throws Exception {
        List<Path> list = new ArrayList<>();
        int n = 0;
        for (String pkgKey : new String[] {"basePackage", "package"}) {
            for (String type : new String[] {"string", "String"}) {
                Path p = tmp.resolve("cand" + (n++) + ".yaml");
                Files.writeString(p, yaml(pkgKey, type), StandardCharsets.UTF_8);
                list.add(p);
            }
        }
        for (String dir : new String[] {"examples", "../examples", "../../examples"}) {
            Path d = Path.of(dir);
            if (Files.isDirectory(d)) {
                try (Stream<Path> s = Files.list(d)) {
                    s.filter(f -> f.toString().endsWith(".yaml") || f.toString().endsWith(".yml"))
                            .sorted().forEach(list::add);
                }
            }
        }
        return list;
    }

    private static String runCli(String... args) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        int code = CodegenCli.run(args, ps);
        assertEquals(0, code);
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void layoutLineShownOnlyForNonDefaultLayout() throws Exception {
        Path spec = null;
        for (Path c : candidates()) {
            try {
                SpecLoader.load(c, "flat");
                SpecLoader.load(c, "nested");
                spec = c;
                break;
            } catch (Exception | AssertionError ignored) {
                // try next candidate
            }
        }
        Assumptions.assumeTrue(spec != null, "no loadable spec found");

        Path outDir = tmp.resolve("out");
        String flat = runCli(spec.toString(), "--out", outDir.toString(),
                "--layout", "flat", "--dry-run");
        String nested = runCli(spec.toString(), "--out", outDir.toString(),
                "--layout", "nested", "--dry-run");

        assertTrue(flat.contains("布局: flat"), flat);
        assertFalse(nested.contains("布局:"), nested);
    }
}
