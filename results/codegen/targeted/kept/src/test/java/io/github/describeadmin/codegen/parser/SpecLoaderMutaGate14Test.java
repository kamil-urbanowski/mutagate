package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpecLoaderMutaGate14Test {

    @Test
    void fieldCommentDefaultsToNameOnlyWhenAbsent() {
        String yaml = """
                basePackage: com.example.demo
                module: project
                entity: Project
                table: biz_project
                fields:
                  - name: projectName
                    type: string
                    length: 64
                    comment: 项目名称
                  - name: remark
                    type: string
                    length: 64
                """;

        ModuleSpec spec = SpecLoaderBridge.parse(new Yaml().load(yaml), "test.yaml");
        List<FieldSpec> fields = spec.fields();

        assertEquals(2, fields.size());
        assertEquals("项目名称", fields.get(0).comment());
        assertEquals("remark", fields.get(1).comment());
    }
}
