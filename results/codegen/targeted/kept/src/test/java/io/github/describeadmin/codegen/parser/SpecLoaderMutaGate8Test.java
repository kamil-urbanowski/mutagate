package io.github.describeadmin.codegen.parser;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate8Test {

    private static Map<String, Object> validRoot() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", "projectName");
        field.put("type", "string");
        field.put("length", 128);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("basePackage", "com.example.demo");
        root.put("module", "project");
        root.put("entity", "Project");
        root.put("table", "biz_project");
        root.put("fields", List.of(field));
        return root;
    }

    @Test
    void missingRequiredKeyIsReported() {
        for (String key : List.of("basePackage", "module", "entity", "table")) {
            Map<String, Object> root = validRoot();
            root.remove(key);

            SpecException ex = assertThrows(SpecException.class,
                    () -> SpecLoader.parse(root, "t.yaml", "nested"),
                    "missing " + key);

            assertEquals(1, ex.errors().size(), "missing " + key + ": " + ex.errors());
            String err = ex.errors().get(0);
            assertTrue(err.startsWith("t.yaml: "), err);
            assertTrue(err.contains("缺少必填项 " + key), err);
        }
    }
}
