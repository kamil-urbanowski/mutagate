package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate7Test {

    @Test
    void requiredFieldsProduceValidationRules() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String page = VueGenerator.page(spec);

        List<FieldSpec> required = spec.fields().stream()
                .filter(f -> !f.nullable())
                .toList();
        assertTrue(!required.isEmpty(), "spec should contain at least one non-nullable field");

        for (FieldSpec f : required) {
            String expected = "  " + f.name() + ": [{ required: true, message: '请填写"
                    + f.comment() + "', trigger: 'blur' }],";
            assertTrue(page.contains(expected), "missing rule line: " + expected);
        }
    }
}
