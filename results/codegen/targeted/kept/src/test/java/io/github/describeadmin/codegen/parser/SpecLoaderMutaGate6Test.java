package io.github.describeadmin.codegen.parser;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate6Test {

    private static Map<String, Object> header() {
        Map<String, Object> root = new HashMap<>();
        root.put("basePackage", "com.example.demo");
        root.put("module", "project");
        root.put("entity", "Project");
        root.put("table", "biz_project");
        return root;
    }

    @Test
    void emptyFieldsErrorOnlyWhenFieldsEmptyAndNoOtherErrors() {
        // 1) fields 为空列表且无其它错误 -> 必须报 "fields 为空"
        Map<String, Object> emptyFields = header();
        emptyFields.put("fields", List.of());
        SpecException ex1 = assertThrows(SpecException.class,
                () -> SpecLoader.parse(emptyFields, "t.yaml", "nested"));
        assertEquals(1, ex1.errors().size());
        assertTrue(ex1.errors().get(0).contains("fields 为空"));

        // 2) 有合法字段 -> 不应抛出，字段被保留
        Map<String, Object> valid = header();
        valid.put("fields", List.of(Map.of("name", "projectName", "type", "string", "length", 128)));
        var spec = SpecLoader.parse(valid, "t.yaml", "nested");
        assertEquals(1, spec.fields().size());

        // 3) 缺少 fields（已有 "缺少 fields" 错误）-> 不应再追加 "fields 为空"
        Map<String, Object> missing = header();
        SpecException ex3 = assertThrows(SpecException.class,
                () -> SpecLoader.parse(missing, "t.yaml", "nested"));
        assertEquals(1, ex3.errors().size());
        assertTrue(ex3.errors().get(0).contains("缺少 fields"));
        assertFalse(ex3.errors().stream().anyMatch(e -> e.contains("fields 为空")));
    }
}
