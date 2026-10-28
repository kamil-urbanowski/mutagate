package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.FieldType;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate1Test {

    @Test
    void pageWithoutQueryFieldsAndWithMixedNullability() throws Exception {
        ModuleSpec base = SpecLoader.load(Path.of("examples/project.yaml"));
        List<FieldSpec> fields = List.of(
                new FieldSpec("title", "title", FieldType.STRING, 50, null, null,
                        false, "标题", FieldSpec.QueryMode.NONE, false),
                new FieldSpec("note", "note", FieldType.TEXT, null, null, null,
                        true, "备注", FieldSpec.QueryMode.NONE, false),
                new FieldSpec("qty", "qty", FieldType.INT, null, null, null,
                        true, "数量", FieldSpec.QueryMode.NONE, false));
        ModuleSpec spec = new ModuleSpec(base.basePackage(), "task", "Task", "t_task",
                "任务", "/api/task", base.layout(), base.layoutOrigin(), fields);

        String page = VueGenerator.page(spec);

        // no search wiring when there are no query fields
        assertFalse(page.contains("useSearchForm"));
        assertFalse(page.contains("SearchFormBar"));
        assertFalse(page.contains("SearchFormSchema"));
        assertTrue(page.contains("const search = reactive({\n  // spec 中没有 query 字段，无查询条件\n});"));

        // imports glue without search lines
        assertTrue(page.contains("import type { Task } from '#/api/task';\n\nimport { onMounted"));
        assertTrue(page.contains("} from '#/api/task';\n\ndefineOptions({ name: 'task' });"));

        // required rule only for non-nullable field
        assertTrue(page.contains("const rules = {\n  title: [{ required: true, message: '请填写标题', trigger: 'blur' }],\n};"));
        assertFalse(page.contains("note: [{ required"));
        assertFalse(page.contains("qty: [{ required"));

        // form model at two indentation levels
        assertTrue(page.contains("const form = reactive({\n  title: '',\n  note: '',\n  qty: undefined as number | undefined,\n});"));
        assertTrue(page.contains("Object.assign(form, {\n    title: '',\n    note: '',\n    qty: undefined as number | undefined,\n  });"));

        // columns: TEXT excluded, others included
        assertTrue(page.contains("<ElTableColumn prop=\"title\" label=\"标题\" min-width=\"140\" />"));
        assertTrue(page.contains("<ElTableColumn prop=\"qty\" label=\"数量\" min-width=\"140\" />"));
        assertFalse(page.contains("<ElTableColumn prop=\"note\""));

        // titles and anchors
        assertTrue(page.contains("<Page description=\"由 codegen 生成，可直接修改\" title=\"任务\">"));
        assertTrue(page.contains(":title=\"editingId === null ? '新增任务' : '编辑任务'\""));
        assertTrue(page.contains("data-testid=\"task-add-btn\""));
        assertTrue(page.contains("data-testid=\"task-table\""));
        assertTrue(page.contains("data-testid=\"task-edit-btn\""));
        assertTrue(page.contains("data-testid=\"task-delete-btn\""));
        assertTrue(page.contains("data-testid=\"task-pagination\""));
        assertTrue(page.contains("data-testid=\"task-form-dialog\""));
        assertTrue(page.contains("data-testid=\"task-cancel-btn\""));
        assertTrue(page.contains("data-testid=\"task-submit-btn\""));
        assertTrue(page.contains("testid=\"task\""));

        // entity-based wiring
        assertTrue(page.contains("const rows = ref<Task[]>([]);"));
        assertTrue(page.contains("function openEdit(row: Task) {"));
        assertTrue(page.contains("function askDelete(row: Task) {"));
        assertTrue(page.contains("await getTaskListApi({ ...page, ...search });"));
        assertTrue(page.contains("? createTaskApi({ ...form })"));
        assertTrue(page.contains(": updateTaskApi(editingId.value, { ...form }));"));
        assertTrue(page.contains("await deleteTaskApi(deletingId.value);"));

        // all-nullable variant gives the placeholder
        List<FieldSpec> nullableOnly = List.of(fields.get(1), fields.get(2));
        ModuleSpec spec2 = new ModuleSpec(base.basePackage(), "task", "Task", "t_task",
                "任务", "/api/task", base.layout(), base.layoutOrigin(), nullableOnly);
        String page2 = VueGenerator.page(spec2);
        assertTrue(page2.contains("const rules = {\n  // spec 中所有字段均可空，无必填校验\n};"));
        assertEquals(-1, page2.indexOf("required: true"));
    }
}
