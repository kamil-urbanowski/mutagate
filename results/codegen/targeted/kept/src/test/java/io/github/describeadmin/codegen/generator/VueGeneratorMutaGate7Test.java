package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.FieldType;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate7Test {

    @Test
    void requiredFieldProducesValidationRule() throws Exception {
        ModuleSpec base = SpecLoader.load(Path.of("examples/project.yaml"));
        FieldSpec required = new FieldSpec(
                "projectName", "project_name", FieldType.STRING, 64, null, null,
                false, "项目名称", FieldSpec.QueryMode.NONE, false);
        ModuleSpec spec = new ModuleSpec(
                base.basePackage(), base.module(), base.entity(), base.table(),
                base.comment(), base.apiPrefix(), base.layout(), base.layoutOrigin(),
                List.of(required));

        String page = VueGenerator.page(spec);

        assertTrue(page.contains(
                "const rules = {\n"
                        + "  projectName: [{ required: true, message: '请填写项目名称', trigger: 'blur' }],\n"
                        + "};"),
                page);
    }
}
