package io.github.describeadmin.codegen.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class SpecLoaderMutaGate2Test {

    @Test
    void invalidModuleNameIsReported() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", "projectName");
        field.put("type", "string");
        field.put("length", 64);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("basePackage", "com.example.demo");
        root.put("module", "Project");
        root.put("entity", "Project");
        root.put("table", "biz_project");
        root.put("fields", List.of(field));

        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(root, "test.yaml", "nested"));

        long moduleErrors = ex.errors().stream()
                .filter(e -> e.contains("module \"Project\"") && e.contains("小写"))
                .count();
        assertEquals(1, moduleErrors);
    }
}
