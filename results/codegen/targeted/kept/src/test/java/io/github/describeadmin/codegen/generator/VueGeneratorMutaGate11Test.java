package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate11Test {

    @Test
    void rulesBlockMatchesRequiredFields() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));

        String rules = spec.fields().stream()
                .filter(f -> !f.nullable())
                .map((FieldSpec f) -> "  " + f.name() + ": [{ required: true, message: '请填写"
                        + f.comment() + "', trigger: 'blur' }],")
                .collect(Collectors.joining("\n"));
        String expected = rules.isEmpty()
                ? "  // spec 中所有字段均可空，无必填校验"
                : rules;

        String page = VueGenerator.page(spec);

        assertTrue(page.contains("const rules = {\n" + expected + "\n};"),
                "rules block should be:\n" + expected + "\nbut page was:\n" + page);
    }
}
