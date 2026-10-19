package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate3Test {

    @Test
    void formModelInitialValuesDependOnFieldType() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String page = VueGenerator.page(spec);

        // STRING -> empty string literal
        assertTrue(page.contains("projectName: '',"), page);
        // LONG -> empty string literal
        assertTrue(page.contains("ownerDeptId: '',"), page);
        // DATE -> empty string literal
        assertTrue(page.contains("startDate: '',"), page);
        // DECIMAL -> undefined number
        assertTrue(page.contains("budget: undefined as number | undefined,"), page);
        // FLAG -> 1
        assertTrue(page.contains("status: 1,"), page);
    }
}
