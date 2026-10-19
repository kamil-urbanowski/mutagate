package io.github.describeadmin.codegen.parser;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate9Test {

    @Test
    void missingModuleIsReportedAsRequired() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", "projectName");
        field.put("type", "string");

        List<Object> fields = new ArrayList<>();
        fields.add(field);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("basePackage", "com.example.demo");
        root.put("entity", "Project");
        root.put("table", "biz_project");
        root.put("apiPrefix", "/api/project");
        root.put("layout", "nested");
        root.put("fields", fields);

        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(root, "test.yaml", null));

        assertEquals(1, ex.errors().size());
        assertTrue(ex.errors().get(0).contains("缺少必填项 module"), ex.errors().get(0));
    }
}
