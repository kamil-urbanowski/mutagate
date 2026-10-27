package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate2Test {

    @Test
    void pageContainsFormModelInitialValuesAtBothIndentLevels() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String page = VueGenerator.page(spec);

        // top-level reactive form (2-space indent)
        assertTrue(page.contains("\n  projectName: '',\n"), page);
        assertTrue(page.contains("\n  budget: undefined as number | undefined,\n"), page);

        // Object.assign in openCreate (4-space indent)
        assertTrue(page.contains("\n    projectName: '',\n"), page);
        assertTrue(page.contains("\n    budget: undefined as number | undefined,\n"), page);
    }
}
