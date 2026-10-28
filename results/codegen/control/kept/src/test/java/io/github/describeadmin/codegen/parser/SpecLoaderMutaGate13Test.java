package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate13Test {

    @Test
    void parseDefaultsOverridesAndSourcePrefixedErrors() {
        // 1. 根节点不是对象：只有一条错误，带 source 前缀
        SpecException rootErr = assertThrows(SpecException.class,
                () -> SpecLoader.parse("not a map", "foo.yaml", "nested"));
        assertEquals(List.of("foo.yaml: 根节点必须是对象（key: value 形式）"), rootErr.errors());

        List<Object> fields = List.of(Map.of("name", "title", "type", "string"));

        // 2. 缺省值：comment 缺省为 entity，apiPrefix 缺省为 /api/ + 模块名下划线转连字符
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("basePackage", "com.example.demo");
        defaults.put("module", "work_order");
        defaults.put("entity", "WorkOrder");
        defaults.put("table", "biz_work_order");
        defaults.put("fields", fields);
        ModuleSpec d = SpecLoader.parse(defaults, "d.yaml", "nested");
        assertEquals("WorkOrder", d.comment());
        assertEquals("/api/work-order", d.apiPrefix());
        assertEquals("--layout 参数", d.layoutOrigin());

        // 3. 显式给出的 comment / apiPrefix 原样保留
        Map<String, Object> explicit = new HashMap<>(defaults);
        explicit.put("comment", "  工单  ");
        explicit.put("apiPrefix", "/custom/path");
        ModuleSpec e = SpecLoader.parse(explicit, "e.yaml", "nested");
        assertEquals("工单", e.comment());
        assertEquals("/custom/path", e.apiPrefix());

        // 4. 缺失必填项：全部报出，且每条都带 source 前缀
        SpecException missing = assertThrows(SpecException.class,
                () -> SpecLoader.parse(new HashMap<String, Object>(), "x.yaml", "nested"));
        List<String> errs = missing.errors();
        assertEquals(5, errs.size());
        for (String err : errs) {
            assertTrue(err.startsWith("x.yaml: "), err);
        }
        assertTrue(errs.get(0).contains("缺少必填项 basePackage"));
        assertTrue(errs.get(3).contains("缺少必填项 table"));
        assertTrue(errs.get(4).contains("缺少 fields"));

        // 5. fields 为空数组且其他都合法：给出专门的"fields 为空"错误
        Map<String, Object> emptyFields = new HashMap<>(defaults);
        emptyFields.put("fields", List.of());
        SpecException empty = assertThrows(SpecException.class,
                () -> SpecLoader.parse(emptyFields, "y.yaml", "nested"));
        assertEquals(1, empty.errors().size());
        assertTrue(empty.errors().get(0).startsWith("y.yaml: fields 为空"));

        // 6. entity 带 Entity 后缀被拦下
        Map<String, Object> suffixed = new HashMap<>(defaults);
        suffixed.put("entity", "OrderEntity");
        SpecException suffix = assertThrows(SpecException.class,
                () -> SpecLoader.parse(suffixed, "z.yaml", "nested"));
        assertEquals(1, suffix.errors().size());
        assertTrue(suffix.errors().get(0).contains("不要带 Entity 后缀"));
    }
}
