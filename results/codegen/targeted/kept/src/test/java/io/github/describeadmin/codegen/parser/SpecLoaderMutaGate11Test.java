package io.github.describeadmin.codegen.parser;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpecLoaderMutaGate11Test {

    @Test
    void missingTableIsReportedAsRequired() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", "projectName");
        field.put("type", "string");
        field.put("length", 128);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("basePackage", "com.example.demo");
        root.put("module", "project");
        root.put("entity", "Project");
        root.put("fields", List.of(field));

        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(root, "test.yaml", "nested"));

        assertTrue(ex.errors().stream().anyMatch(e -> e.contains("缺少必填项 table")),
                "errors should report missing table: " + ex.errors());
    }
}
