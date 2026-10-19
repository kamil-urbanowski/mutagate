package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate6Test {

    @Test
    void requiredRulesOnlyForNonNullableFields() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String page = VueGenerator.page(spec);

        for (FieldSpec f : spec.fields()) {
            String rule = "  " + f.name() + ": [{ required: true, message: '请填写"
                    + f.comment() + "', trigger: 'blur' }],";
            if (f.nullable()) {
                assertFalse(page.contains(rule),
                        "nullable field must not have a required rule: " + f.name());
            } else {
                assertTrue(page.contains(rule),
                        "non-nullable field must have a required rule: " + f.name());
            }
        }
    }
}
