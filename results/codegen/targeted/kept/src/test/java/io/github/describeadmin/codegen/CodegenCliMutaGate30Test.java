package io.github.describeadmin.codegen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate30Test {

    private static final String HINT = "提示：Service / Controller";

    @TempDir
    Path tmp;

    private static Path findExampleSpec() throws Exception {
        Path dir = Path.of("").toAbsolutePath();
        for (int i = 0; i < 5 && dir != null; i++, dir = dir.getParent()) {
            Path ex = dir.resolve("examples");
            if (Files.isDirectory(ex)) {
                try (Stream<Path> s = Files.list(ex)) {
                    List<Path> specs = s.filter(p -> {
                        String n = p.getFileName().toString();
                        return n.endsWith(".yaml") || n.endsWith(".yml");
                    }).sorted().toList();
                    if (!specs.isEmpty()) {
                        return specs.get(0);
                    }
                }
            }
        }
        return null;
    }

    private String runCli(String... args) throws Exception {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        int rc = CodegenCli.run(args, ps);
        assertEquals(0, rc);
        return buf.toString(StandardCharsets.UTF_8);
    }

    @Test
    void hintIsPrintedOnlyWhenFilesWereSkippedAndNotDryRun() throws Exception {
        Path spec = findExampleSpec();
        if (spec == null) {
            spec = tmp.resolve("spec.yaml");
            Files.writeString(spec, """
                    package: com.example
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
        }
        Path outDir = tmp.resolve("out");
        String specArg = spec.toString();
        String outArg = outDir.toString();

        // nothing skipped, dry run: no hint
        String dry = runCli(specArg, "--out", outArg, "--dry-run");
        assertFalse(dry.contains(HINT));

        // first real run: nothing exists yet, nothing skipped: no hint
        String first = runCli(specArg, "--out", outArg);
        assertFalse(first.contains(HINT));

        // second real run: everything exists and is skipped: hint shown
        String second = runCli(specArg, "--out", outArg);
        assertTrue(second.contains(HINT));

        // skipped files but dry run: no hint
        String dryAgain = runCli(specArg, "--out", outArg, "--dry-run");
        assertFalse(dryAgain.contains(HINT));
    }
}
