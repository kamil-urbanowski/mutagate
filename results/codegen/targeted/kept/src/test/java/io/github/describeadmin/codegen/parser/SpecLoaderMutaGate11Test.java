package io.github.describeadmin.codegen.parser;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate11Test {

    @Test
    void missingTableIsReportedAsRequired() {
        Map<String, Object> root = Map.of(
                "basePackage", "com.example.demo",
                "module", "project",
                "entity", "Project",
                "fields", List.of(Map.of("name", "projectName", "type", "string")));

        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(root, "test.yaml", "nested"));

        List<String> errors = ex.errors();
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("缺少必填项 table"), errors.get(0));
        assertTrue(errors.get(0).startsWith("test.yaml: "), errors.get(0));
    }
}
