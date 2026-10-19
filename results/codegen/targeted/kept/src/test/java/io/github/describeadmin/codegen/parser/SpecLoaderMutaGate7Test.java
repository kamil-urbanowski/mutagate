package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpecLoaderMutaGate7Test {

    private static Map<String, Object> baseSpec() {
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
    void commentDefaultsToEntityOnlyWhenAbsent() {
        // comment absent -> falls back to the entity name
        ModuleSpec withoutComment = SpecLoader.parse(baseSpec(), "test.yaml", "nested");
        assertEquals("Project", withoutComment.comment());

        // comment present -> used as is
        Map<String, Object> spec = baseSpec();
        spec.put("comment", "项目管理");
        ModuleSpec withComment = SpecLoader.parse(spec, "test.yaml", "nested");
        assertEquals("项目管理", withComment.comment());
    }
}
