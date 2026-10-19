package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate9Test {

    @Test
    void searchModelContainsRangeQueryFields() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String page = VueGenerator.page(spec);

        assertFalse(page.contains("// spec 中没有 query 字段，无查询条件"));
        assertTrue(page.contains("  startDateEnd: undefined as string | undefined,"));
        assertTrue(page.contains("  startDateStart: undefined as string | undefined,"));
        assertTrue(page.indexOf("startDateEnd: undefined as")
                < page.indexOf("startDateStart: undefined as"));
    }
}
