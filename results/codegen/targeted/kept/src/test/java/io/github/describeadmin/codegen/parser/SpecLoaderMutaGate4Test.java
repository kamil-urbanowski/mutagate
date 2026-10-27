package io.github.describeadmin.codegen.parser;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate4Test {

    @Test
    void entityWithEntitySuffixIsRejectedWithSuffixMessage() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", "projectName");
        field.put("type", "string");

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("basePackage", "com.example.demo");
        root.put("module", "project");
        root.put("entity", "ProjectEntity");
        root.put("table", "biz_project");
        root.put("fields", List.of(field));

        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(root, "test.yaml", "nested"));

        List<String> suffixErrors = ex.errors().stream()
                .filter(e -> e.contains("不要带 Entity 后缀"))
                .toList();
        assertEquals(1, suffixErrors.size());
        assertTrue(suffixErrors.get(0).contains("ProjectEntity"));
        assertTrue(suffixErrors.get(0).startsWith("test.yaml: "));
    }
}
