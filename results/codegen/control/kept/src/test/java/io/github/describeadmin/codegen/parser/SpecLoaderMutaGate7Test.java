package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate7Test {

    @Test
    void parseDefaultsOverridesAndErrorPrefixes() {
        // 1. 缺省值：comment 取 entity，apiPrefix 把下划线换成连字符，字段缺省属性
        String defaults = """
                basePackage: com.example.demo
                module: work_order
                entity: WorkOrder
                table: biz_work_order
                fields:
                  - name: workNo
                    type: int
                """;
        ModuleSpec s = SpecLoader.parse(new Yaml().load(defaults), "a.yaml", "nested");
        assertEquals("WorkOrder", s.comment());
        assertEquals("/api/work-order", s.apiPrefix());
        assertEquals("work_order", s.module());
        FieldSpec f = s.fields().get(0);
        assertEquals("work_no", f.column());
        assertEquals("workNo", f.comment());
        assertTrue(f.nullable());
        assertFalse(f.indexed());
        assertEquals(FieldSpec.QueryMode.NONE, f.query());
        assertNull(f.length());

        // 2. 显式 comment / apiPrefix 优先
        String explicit = defaults + "comment: 工单\napiPrefix: /custom/wo\n";
        ModuleSpec e = SpecLoader.parse(new Yaml().load(explicit), "a.yaml", "nested");
        assertEquals("工单", e.comment());
        assertEquals("/custom/wo", e.apiPrefix());

        // 3. 根节点不是对象：单条错误，带 source 前缀
        SpecException notMap = assertThrows(SpecException.class,
                () -> SpecLoader.parse(List.of("x"), "root.yaml", "nested"));
        assertEquals(List.of("root.yaml: 根节点必须是对象（key: value 形式）"), notMap.errors());

        // 4. 全部缺失：每条错误都带 source 前缀，并包含必填项与 fields 提示
        SpecException missing = assertThrows(SpecException.class,
                () -> SpecLoader.parse(new Yaml().load("comment: x\n"), "src.yaml", "nested"));
        assertTrue(missing.errors().size() >= 5);
        for (String err : missing.errors()) {
            assertTrue(err.startsWith("src.yaml: "), err);
        }
        String joined = String.join("\n", missing.errors());
        assertTrue(joined.contains("缺少必填项 basePackage"));
        assertTrue(joined.contains("缺少必填项 module"));
        assertTrue(joined.contains("缺少必填项 entity"));
        assertTrue(joined.contains("缺少必填项 table"));
        assertTrue(joined.contains("缺少 fields"));

        // 5. fields 为空数组且其余合法：给出专门提示
        String emptyFields = """
                basePackage: com.example.demo
                module: project
                entity: Project
                table: biz_project
                fields: []
                """;
        SpecException empty = assertThrows(SpecException.class,
                () -> SpecLoader.parse(new Yaml().load(emptyFields), "e.yaml", "nested"));
        assertEquals(1, empty.errors().size());
        assertTrue(empty.errors().get(0).startsWith("e.yaml: fields 为空"));

        // 6. entity 带 Entity 后缀被拦下
        String suffix = defaults.replace("entity: WorkOrder", "entity: WorkOrderEntity");
        SpecException se = assertThrows(SpecException.class,
                () -> SpecLoader.parse(new Yaml().load(suffix), "s.yaml", "nested"));
        assertTrue(String.join("\n", se.errors()).contains("不要带 Entity 后缀"));

        // 7. 非法包名被拦下
        String badPkg = defaults.replace("com.example.demo", "Com.Example");
        SpecException pe = assertThrows(SpecException.class,
                () -> SpecLoader.parse(new Yaml().load(badPkg), "p.yaml", "nested"));
        assertTrue(String.join("\n", pe.errors()).contains("不是合法包名"));
    }
}
