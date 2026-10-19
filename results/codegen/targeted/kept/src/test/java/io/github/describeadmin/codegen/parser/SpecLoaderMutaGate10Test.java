package io.github.describeadmin.codegen.parser;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate10Test {

    @Test
    void missingEntityIsReportedAsRequired() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", "projectName");
        field.put("type", "string");
        field.put("length", 64);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("basePackage", "com.example.demo");
        root.put("module", "project");
        root.put("table", "biz_project");
        root.put("fields", List.of(field));

        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(root, "test.yaml", "nested"));

        List<String> errors = ex.errors();
        long entityErrors = errors.stream()
                .filter(e -> e.contains("缺少必填项 entity"))
                .count();
        assertEquals(1, entityErrors, "errors: " + errors);
        assertTrue(errors.stream().allMatch(e -> e.startsWith("test.yaml: ")));
    }
}
