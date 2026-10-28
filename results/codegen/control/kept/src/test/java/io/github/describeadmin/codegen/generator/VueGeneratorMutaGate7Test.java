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

class VueGeneratorMutaGate7Test {

    private static ModuleSpec withFields(ModuleSpec base, List<FieldSpec> fields) {
        return new ModuleSpec(base.basePackage(), base.module(), base.entity(), base.table(),
                base.comment(), base.apiPrefix(), base.layout(), base.layoutOrigin(), fields);
    }

    private static FieldSpec field(String name, String column, FieldType type, boolean nullable, String comment) {
        return new FieldSpec(name, column, type, type == FieldType.STRING ? 64 : null, null, null,
                nullable, comment, FieldSpec.QueryMode.NONE, false);
    }

    @Test
    void pageWithoutQueryFieldsAndRequiredRules() throws Exception {
        ModuleSpec base = SpecLoader.load(Path.of("examples/project.yaml"));

        FieldSpec name = field("projectName", "project_name", FieldType.STRING, false, "项目名称");
        FieldSpec count = field("headCount", "head_count", FieldType.INT, true, "人数");
        FieldSpec remark = field("remark", "remark", FieldType.TEXT, true, "备注");

        String page = VueGenerator.page(withFields(base, List.of(name, count, remark)));

        // no query fields: no search wiring, placeholder in search model
        assertFalse(page.contains("useSearchForm"));
        assertFalse(page.contains("SearchFormBar"));
        assertTrue(page.contains("// spec 中没有 query 字段，无查询条件"));

        // required rule only for non-nullable field
        assertTrue(page.contains("const rules = {\n  projectName: [{ required: true, message: '请填写项目名称', trigger: 'blur' }],\n};"));
        assertFalse(page.contains("headCount: [{ required"));
        assertFalse(page.contains("所有字段均可空"));

        // initial values at both indentation levels
        assertTrue(page.contains("\n  projectName: '',"));
        assertTrue(page.contains("\n    projectName: '',"));
        assertTrue(page.contains("\n  headCount: undefined as number | undefined,"));
        assertTrue(page.contains("\n    headCount: undefined as number | undefined,"));
        assertTrue(page.contains("\n  remark: '',"));

        // all-nullable variant yields the rules placeholder
        String relaxed = VueGenerator.page(withFields(base, List.of(count, remark)));
        assertTrue(relaxed.contains("// spec 中所有字段均可空，无必填校验"));
        assertFalse(relaxed.contains("required: true"));
    }
}
