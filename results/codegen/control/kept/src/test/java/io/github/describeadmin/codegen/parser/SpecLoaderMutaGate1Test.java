package io.github.describeadmin.codegen.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.describeadmin.codegen.model.ModuleSpec;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SpecLoaderMutaGate1Test {

    private static Map<String, Object> baseRoot() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", "orderNo");
        field.put("type", "string");
        field.put("length", 32);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("basePackage", "com.example.demo");
        root.put("module", "work_order");
        root.put("entity", "WorkOrder");
        root.put("table", "biz_work_order");
        root.put("fields", List.of(field));
        return root;
    }

    @Test
    void defaultsExplicitValuesAndErrorPrefixing() {
        // Defaults: comment falls back to entity, apiPrefix derives from module with '_' -> '-'
        ModuleSpec defaults = SpecLoader.parse(baseRoot(), "a.yaml", "nested");
        assertEquals("WorkOrder", defaults.comment());
        assertEquals("/api/work-order", defaults.apiPrefix());
        assertEquals("orderNo", defaults.fields().get(0).comment());
        assertEquals("order_no", defaults.fields().get(0).column());
        assertEquals("--layout 参数", defaults.layoutOrigin());

        // Explicit values are kept (and trimmed)
        Map<String, Object> explicit = baseRoot();
        explicit.put("comment", "  工单  ");
        explicit.put("apiPrefix", "/custom/orders");
        ModuleSpec given = SpecLoader.parse(explicit, "a.yaml", "nested");
        assertEquals("工单", given.comment());
        assertEquals("/custom/orders", given.apiPrefix());

        // Non-map root: single error with source prefix
        SpecException notMap = assertThrows(SpecException.class,
                () -> SpecLoader.parse("just a string", "root.yaml", "nested"));
        assertEquals(List.of("root.yaml: 根节点必须是对象（key: value 形式）"), notMap.errors());

        // Empty map: all required keys missing plus missing fields, each prefixed with source
        SpecException empty = assertThrows(SpecException.class,
                () -> SpecLoader.parse(new HashMap<String, Object>(), "my.yaml", "nested"));
        assertEquals(5, empty.errors().size());
        for (String e : empty.errors()) {
            assertTrue(e.startsWith("my.yaml: "), e);
        }
        assertTrue(empty.errors().contains("my.yaml: 缺少 fields"));
        assertTrue(empty.errors().stream().anyMatch(e -> e.contains("缺少必填项 basePackage")));
        assertTrue(empty.errors().stream().anyMatch(e -> e.contains("缺少必填项 table")));
    }
}
