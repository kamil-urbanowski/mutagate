package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate10Test {

    @Test
    void searchModelIsRenderedWhenQueryFieldsExist() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String page = VueGenerator.page(spec);

        // The spec has a range query field (startDate): both bounds must appear in the reactive search model.
        assertTrue(page.contains("  startDateStart: undefined as string | undefined,"),
                "search model should contain the range start entry");
        assertTrue(page.contains("  startDateEnd: undefined as string | undefined,"),
                "search model should contain the range end entry");
        // The placeholder comment is only for specs without query fields.
        assertFalse(page.contains("无查询条件"),
                "placeholder comment must not appear when query fields exist");
    }
}
