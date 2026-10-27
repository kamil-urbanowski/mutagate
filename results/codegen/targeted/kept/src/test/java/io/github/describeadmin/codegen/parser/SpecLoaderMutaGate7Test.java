package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpecLoaderMutaGate7Test {

    private static Map<String, Object> baseSpec() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("basePackage", "com.example.demo");
        root.put("module", "project");
        root.put("entity", "Project");
        root.put("table", "biz_project");
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", "projectName");
        field.put("type", "string");
        field.put("length", 64);
        List<Object> fields = new ArrayList<>();
        fields.add(field);
        root.put("fields", fields);
        return root;
    }

    @Test
    void commentDefaultsToEntityOnlyWhenAbsent() {
        // comment absent -> falls back to the entity name
        Map<String, Object> without = baseSpec();
        ModuleSpec a = SpecLoader.parse(without, "test.yaml", "nested");
        assertEquals("Project", a.comment());

        // comment present -> used as-is
        Map<String, Object> with = baseSpec();
        with.put("comment", "项目管理");
        ModuleSpec b = SpecLoader.parse(with, "test.yaml", "nested");
        assertEquals("项目管理", b.comment());
    }
}
