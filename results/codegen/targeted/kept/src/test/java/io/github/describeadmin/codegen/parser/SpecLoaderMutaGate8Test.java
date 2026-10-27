package io.github.describeadmin.codegen.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SpecLoaderMutaGate8Test {

    @Test
    void missingBasePackageIsReportedAsRequired() {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", "projectName");
        field.put("type", "string");
        field.put("length", 128);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("module", "project");
        root.put("entity", "Project");
        root.put("table", "biz_project");
        root.put("fields", List.of(field));

        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(root, "test.yaml"));

        assertTrue(ex.errors().stream().anyMatch(e -> e.contains("缺少必填项 basePackage")),
                "errors should report the missing basePackage: " + ex.errors());
    }
}
