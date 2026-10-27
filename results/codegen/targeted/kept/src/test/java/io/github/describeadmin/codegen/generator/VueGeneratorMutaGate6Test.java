package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.FieldType;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate6Test {

    @Test
    void requiredRulesOnlyForNonNullableFields() throws Exception {
        ModuleSpec base = SpecLoader.load(Path.of("examples/project.yaml"));

        FieldSpec required = new FieldSpec(
                "title", "title", FieldType.STRING, 50, null, null,
                false, "标题名", FieldSpec.QueryMode.NONE, false);
        FieldSpec optional = new FieldSpec(
                "note", "note", FieldType.STRING, 50, null, null,
                true, "备注说明", FieldSpec.QueryMode.NONE, false);

        ModuleSpec spec = new ModuleSpec(
                base.basePackage(), base.module(), base.entity(), base.table(),
                base.comment(), base.apiPrefix(), base.layout(), base.layoutOrigin(),
                List.of(required, optional));

        String page = VueGenerator.page(spec);

        assertTrue(page.contains(
                "  title: [{ required: true, message: '请填写标题名', trigger: 'blur' }],"));
        assertFalse(page.contains("note: [{ required"));
    }
}
