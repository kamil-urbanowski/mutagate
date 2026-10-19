package io.github.describeadmin.codegen;

import io.github.describeadmin.codegen.model.Layout;
import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate10Test {

    @Test
    void planReturnsAllEightFilesWithExpectedPaths() {
        ModuleSpec spec = new ModuleSpec(
                "com.example", "book", "Book", "book", "Book",
                "/book", Layout.NESTED, "default", List.of());
        Path out = Path.of("out");
        Path fe = Path.of("fe");

        Map<Path, String> files = CodegenCli.plan(spec, out, fe);

        assertFalse(files.isEmpty());
        assertEquals(8, files.size());
        assertTrue(files.containsKey(out.resolve("src/main/resources/db").resolve("schema-book.sql")));
        assertTrue(files.containsKey(out.resolve("src/main/resources/db").resolve("menu-book.sql")));
        assertTrue(files.containsKey(fe.resolve("src/api").resolve("book.ts")));
        assertTrue(files.containsKey(fe.resolve("src/views").resolve("book").resolve("index.vue")));
    }
}
