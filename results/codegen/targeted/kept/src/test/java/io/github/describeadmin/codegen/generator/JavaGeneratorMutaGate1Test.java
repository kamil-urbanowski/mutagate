package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaGeneratorMutaGate1Test {

    @Test
    void controllerJavadocListsQueryConditionsWhenQueryFieldsExist() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        assertTrue(!spec.queryFields().isEmpty());

        String src = JavaGenerator.controller(spec);

        assertTrue(src.contains("<p>列表查询支持的条件："), src);
        assertTrue(src.contains(" * <ul>\n"), src);
        assertTrue(src.contains(" *   <li>{@code projectName} —— "), src);
        assertTrue(src.contains("（like）</li>"), src);
        assertTrue(src.contains(" * </ul>\n"), src);
    }
}
