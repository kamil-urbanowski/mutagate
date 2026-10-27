package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate13Test {

    private static String yaml(String column) {
        return """
                basePackage: com.example.demo
                module: project
                entity: Project
                table: biz_project
                fields:
                  - name: projectName
                    type: string
                    length: 64
                    column: %s
                """.formatted(column);
    }

    @Test
    void explicitColumnIsValidatedAgainstSnakeCase() {
        // A valid explicit column must be accepted and kept as given.
        ModuleSpec ok = SpecLoader.parse(new Yaml().load(yaml("custom_col")), "t.yaml");
        assertEquals("custom_col", ok.fields().get(0).column());

        // An invalid explicit column must be rejected with the column-format error.
        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(new Yaml().load(yaml("BadCol")), "t.yaml"));
        assertTrue(ex.errors().stream()
                .anyMatch(e -> e.contains("column \"BadCol\" 应为小写下划线形式")));
    }
}
