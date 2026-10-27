package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate9Test {

    @Test
    void pageSearchModelContainsRangeQueryEntries() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));

        String page = VueGenerator.page(spec);

        String end = "  startDateEnd: undefined as string | undefined,";
        String start = "  startDateStart: undefined as string | undefined,";

        assertTrue(page.contains(end), "range End entry missing in search model");
        assertTrue(page.contains(start), "range Start entry missing in search model");
        assertTrue(page.indexOf(end) < page.indexOf(start), "End should precede Start");
        assertFalse(page.contains("// spec 中没有 query 字段，无查询条件"),
                "placeholder must not appear when query fields exist");
        assertEquals(1, page.split("startDateEnd: undefined as string \\| undefined,", -1).length - 1);
    }
}
