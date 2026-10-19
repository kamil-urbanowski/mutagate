package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuSqlGeneratorMutaGate2Test {

    @Test
    void generatedSqlStartsWithHeaderContainingCommentAndComponentPath() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));

        String sql = MenuSqlGenerator.generate(spec);

        assertTrue(sql.startsWith("-- ===="), "SQL should start with the header banner");
        assertTrue(sql.contains("-- " + spec.comment() + " 菜单与权限点"),
                "header should contain the module comment title");
        assertTrue(sql.contains("'" + spec.module() + "/index' 对应 frontend 的 src/views/"
                        + spec.module() + "/index.vue"),
                "header should document the component path");
    }
}
