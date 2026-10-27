package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate4Test {

    @Test
    void formModelRendersInitialValuesAtBothIndentLevels() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String page = VueGenerator.page(spec);

        // top-level reactive form: 2-space indent
        assertTrue(page.contains("\n  budget: undefined as number | undefined,\n"),
                "top-level form model should contain budget initial value");
        assertTrue(page.contains("\n  ownerDeptId: '',\n"),
                "top-level form model should contain ownerDeptId initial value");

        // Object.assign inside openCreate: 4-space indent
        assertTrue(page.contains("\n    budget: undefined as number | undefined,\n"),
                "openCreate reset should contain budget initial value");
        assertTrue(page.contains("\n    ownerDeptId: '',\n"),
                "openCreate reset should contain ownerDeptId initial value");
    }
}
