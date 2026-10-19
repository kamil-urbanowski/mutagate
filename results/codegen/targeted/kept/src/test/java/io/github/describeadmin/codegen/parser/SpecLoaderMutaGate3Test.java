package io.github.describeadmin.codegen.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SpecLoaderMutaGate3Test {

    private static Map<String, Object> spec(String entity) {
        Map<String, Object> field = new HashMap<>();
        field.put("name", "projectName");
        field.put("type", "string");
        field.put("length", 64);

        Map<String, Object> root = new HashMap<>();
        root.put("basePackage", "com.example.demo");
        root.put("module", "project");
        root.put("entity", entity);
        root.put("table", "biz_project");
        root.put("fields", List.of(field));
        root.put("layout", "nested");
        return root;
    }

    @Test
    void entityMustBeUpperCamel() {
        // A valid entity name must not be reported by the UPPER_CAMEL check.
        assertEquals("Project", SpecLoader.parse(spec("Project"), "t.yaml", "nested").entity());

        // A lower-camel entity name must be reported.
        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(spec("project"), "t.yaml", "nested"));
        assertTrue(ex.errors().stream()
                        .anyMatch(e -> e.contains("entity \"project\"") && e.contains("应为大驼峰")),
                "errors: " + ex.errors());
    }
}
