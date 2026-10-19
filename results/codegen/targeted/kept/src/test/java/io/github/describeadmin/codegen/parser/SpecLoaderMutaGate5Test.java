package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    void tableNameFormatIsValidatedExactlyWhenInvalid() {
        // a valid snake_case table must not produce any error
        ModuleSpec ok = assertDoesNotThrow(
                () -> SpecLoader.parse(spec("biz_project"), "t.yaml", "nested"));
        assertEquals("biz_project", ok.table());

        // an invalid table name must be reported with the specific message
        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(spec("BizProject"), "t.yaml", "nested"));
        assertEquals(1, ex.errors().size());
        assertTrue(ex.errors().get(0).contains(
                "t.yaml: table \"BizProject\" 应为小写下划线形式，如 biz_project"));
    }
}
