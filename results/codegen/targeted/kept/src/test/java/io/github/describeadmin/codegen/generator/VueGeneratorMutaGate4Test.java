package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate4Test {

    @Test
    void formModelLinesAppearAtBothIndentLevels() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String page = VueGenerator.page(spec);

        // long field -> initial value '' ; only present in the form model, not the search model
        assertTrue(page.contains("\n  ownerDeptId: '',"),
                "top-level reactive form should contain ownerDeptId initial value");
        assertTrue(page.contains("\n    ownerDeptId: '',"),
                "Object.assign block should contain indented ownerDeptId initial value");
    }
}
