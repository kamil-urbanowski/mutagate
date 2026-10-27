package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaGeneratorMutaGate2Test {

    @Test
    void controllerWithQueryFieldsImportsWrapperTypesAndMap() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        assertFalse(spec.queryFields().isEmpty(), "precondition: spec has query fields");

        String src = JavaGenerator.controller(spec);

        assertTrue(src.contains("import com.baomidou.mybatisplus.core.conditions.Wrapper;"));
        assertTrue(src.contains("import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;"));
        assertTrue(src.contains("import java.util.Map;"));
        assertTrue(src.contains("buildListWrapper(Map<String, String> params)"));
    }
}
