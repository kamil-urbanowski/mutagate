package io.github.describeadmin.codegen.parser;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate5Test {

    private static Map<String, Object> spec(String table) {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", "projectName");
        field.put("type", "string");
        field.put("length", 64);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("basePackage", "com.example.demo");
        root.put("module", "project");
        root.put("entity", "Project");
        root.put("table", table);
        root.put("fields", List.of(field));
        return root;
    }

    @Test
    void tableNameMustBeLowerSnakeCase() {
        // Valid table name: no error, parse succeeds (layout override avoids env dependence)
        var ok = SpecLoader.parse(spec("biz_project"), "t.yaml", "nested");
        assertEquals("biz_project", ok.table());

        // Invalid table name: must be rejected with the snake-case message
        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(spec("BizProject"), "t.yaml", "nested"));
        assertTrue(ex.errors().stream()
                        .anyMatch(e -> e.contains("table \"BizProject\" 应为小写下划线形式")),
                "errors: " + ex.errors());
        assertEquals(1, ex.errors().size(), "errors: " + ex.errors());
    }
}
