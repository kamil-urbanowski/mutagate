package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate11Test {

    @Test
    void rulesBlockReflectsRequiredFields() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String page = VueGenerator.page(spec);

        String placeholder = "// spec 中所有字段均可空，无必填校验";
        List<FieldSpec> required = spec.fields().stream()
                .filter(f -> !f.nullable())
                .toList();

        if (required.isEmpty()) {
            assertTrue(page.contains(placeholder));
        } else {
            assertFalse(page.contains(placeholder));
            for (FieldSpec f : required) {
                String line = "  " + f.name() + ": [{ required: true, message: '请填写"
                        + f.comment() + "', trigger: 'blur' }],";
                assertTrue(page.contains(line), "missing rule: " + line);
            }
        }
    }
}
