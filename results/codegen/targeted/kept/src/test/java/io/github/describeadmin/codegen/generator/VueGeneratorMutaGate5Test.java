package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate5Test {

    @Test
    void requiredRulesGeneratedOnlyForNonNullableFields() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String page = VueGenerator.page(spec);

        assertFalse(spec.fields().isEmpty());

        int requiredCount = 0;
        for (FieldSpec f : spec.fields()) {
            String rule = "  " + f.name() + ": [{ required: true, message: '请填写"
                    + f.comment() + "', trigger: 'blur' }],";
            if (f.nullable()) {
                assertFalse(page.contains(rule), "nullable field must have no rule: " + f.name());
            } else {
                requiredCount++;
                assertTrue(page.contains(rule), "non-nullable field must have rule: " + f.name());
            }
        }

        int occurrences = page.split("required: true", -1).length - 1;
        assertEquals(requiredCount, occurrences);
        assertEquals(requiredCount == 0, page.contains("spec 中所有字段均可空，无必填校验"));
    }
}
