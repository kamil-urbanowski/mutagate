package io.github.describeadmin.codegen.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SpecLoaderMutaGate9Test {

    @Test
    void missingModuleIsReportedAsRequired() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", "projectName");
        field.put("type", "string");
        field.put("length", 64);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("basePackage", "com.example.demo");
        root.put("entity", "Project");
        root.put("table", "biz_project");
        root.put("fields", List.of(field));

        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(root, "test.yaml", "nested"));

        List<String> errors = ex.errors();
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("缺少必填项 module"), errors.get(0));
    }
}
