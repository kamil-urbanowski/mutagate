package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate13Test {

    private static final String TEMPLATE = """
            basePackage: com.example.demo
            module: project
            entity: Project
            table: biz_project
            fields:
              - name: projectName
                column: %s
                type: string
            """;

    @Test
    void explicitColumnIsValidatedAgainstSnakeCase() {
        // 合法的显式列名：不应报错，且原样保留
        ModuleSpec ok = SpecLoader.parse(
                new Yaml().load(TEMPLATE.formatted("custom_col")), "t.yaml");
        assertEquals("custom_col", ok.fields().get(0).column());

        // 非法的显式列名：必须报出格式错误
        SpecException ex = assertThrows(SpecException.class, () -> SpecLoader.parse(
                new Yaml().load(TEMPLATE.formatted("BadCol")), "t.yaml"));
        assertTrue(ex.errors().stream()
                        .anyMatch(e -> e.contains("column \"BadCol\" 应为小写下划线形式")),
                "应报告非法列名: " + ex.errors());
    }
}
