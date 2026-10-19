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

        // top-level reactive form (indent of 2 spaces)
        assertTrue(page.contains("\n  projectName: '',"), page);
        assertTrue(page.contains("\n  budget: undefined as number | undefined,"), page);
        // inside openCreate's Object.assign (indent of 4 spaces)
        assertTrue(page.contains("\n    projectName: '',"), page);
        assertTrue(page.contains("\n    budget: undefined as number | undefined,"), page);
    }
}
