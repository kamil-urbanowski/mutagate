package io.github.describeadmin.codegen.parser;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpecLoaderMutaGate2Test {

    private static String spec(String module) {
        return """
                basePackage: com.example.demo
                module: %s
                entity: Project
                table: biz_project
                layout: flat
                fields:
                  - name: projectName
                    type: string
                    length: 64
                """.formatted(module);
    }

    @Test
    void moduleNameValidationFlagsOnlyInvalidModules() {
        // 非法 module（大写开头）必须产生恰好一条 module 错误
        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoaderBridge.parse(new Yaml().load(spec("Project")), "t.yaml"));
        List<String> moduleErrors = ex.errors().stream()
                .filter(e -> e.contains("module \"Project\""))
                .toList();
        assertEquals(1, moduleErrors.size());

        // 合法 module 不应产生任何错误
        assertEquals("work_order",
                SpecLoaderBridge.parse(new Yaml().load(spec("work_order")), "t.yaml").module());
    }
}
