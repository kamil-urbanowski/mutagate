package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate12Test {

    private static Map<String, Object> spec(int length) {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", "projectName");
        field.put("type", "string");
        field.put("length", length);
        field.put("indexed", true);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("basePackage", "com.example.demo");
        root.put("module", "project");
        root.put("entity", "Project");
        root.put("table", "biz_project");
        root.put("fields", List.of(field));
        return root;
    }

    @Test
    void indexedVarcharBoundaryIs191Inclusive() {
        // Exactly 191 is allowed: must not be rejected.
        ModuleSpec ok = SpecLoader.parse(spec(191), "test.yaml", "nested");
        assertEquals(1, ok.fields().size());
        assertEquals(191, ok.fields().get(0).length());

        // 192 is over the limit: must be rejected.
        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(spec(192), "test.yaml", "nested"));
        assertTrue(ex.getMessage().contains("767"));
    }
}
