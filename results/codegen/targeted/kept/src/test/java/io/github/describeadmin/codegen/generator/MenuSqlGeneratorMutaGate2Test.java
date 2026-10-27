package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuSqlGeneratorMutaGate2Test {

    @Test
    void headerIsEmittedAtStartWithCommentAndComponentHint() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));

        String sql = MenuSqlGenerator.generate(spec);

        assertTrue(sql.startsWith("-- ====="), "SQL 应以头部注释分隔线开头");
        assertTrue(sql.contains("-- " + spec.comment() + " 菜单与权限点"),
                "头部应包含模块注释标题");
        assertTrue(sql.contains("'" + spec.module() + "/index' 对应 frontend 的 src/views/"
                        + spec.module() + "/index.vue"),
                "头部应包含 component 路径提示");
    }
}
