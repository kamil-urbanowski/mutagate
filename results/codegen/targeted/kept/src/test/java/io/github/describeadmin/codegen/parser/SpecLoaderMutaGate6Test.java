package io.github.describeadmin.codegen.parser;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate6Test {

    @Test
    void emptyFieldsReportedOnlyWhenNoOtherErrors() {
        String header = """
                basePackage: com.example.demo
                module: project
                entity: Project
                table: biz_project
                fields: []
                """;
        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoaderBridge.parse(new Yaml().load(header), "t.yaml", "nested"));
        assertEquals(1, ex.errors().size());
        assertTrue(ex.errors().get(0).contains("fields 为空"));

        String withOtherError = """
                module: project
                entity: Project
                table: biz_project
                fields: []
                """;
        SpecException ex2 = assertThrows(SpecException.class,
                () -> SpecLoaderBridge.parse(new Yaml().load(withOtherError), "t.yaml", "nested"));
        assertEquals(1, ex2.errors().size());
        assertTrue(ex2.errors().get(0).contains("basePackage"));
        assertTrue(ex2.errors().stream().noneMatch(e -> e.contains("fields 为空")));
    }
}
