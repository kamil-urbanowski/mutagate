package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate8Test {

    @Test
    void rangeQueryFieldProducesStartAndEndSearchModelEntries() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));

        String page = VueGenerator.page(spec);

        String end = "  startDateEnd: undefined as string | undefined,";
        String start = "  startDateStart: undefined as string | undefined,";
        String plain = "  startDate: undefined as string | undefined,";

        assertTrue(page.contains(end), "range field should yield an End entry");
        assertTrue(page.contains(start), "range field should yield a Start entry");
        assertTrue(page.indexOf(end) < page.indexOf(start), "End entry should precede Start entry");
        assertFalse(page.contains(plain), "range field must not yield a plain entry");
    }
}
