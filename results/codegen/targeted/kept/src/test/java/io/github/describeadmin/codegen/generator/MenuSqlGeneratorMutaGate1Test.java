package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuSqlGeneratorMutaGate1Test {

    @Test
    void generatedSqlGrantsMenusToAdminRole() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));

        String sql = MenuSqlGenerator.generate(spec);

        assertTrue(sql.contains("INSERT INTO sys_role_menu (role_id, menu_id)"),
                "应包含授权插入语句");
        assertTrue(sql.contains("WHERE r.role_code = 'ADMIN' AND r.deleted = 0"),
                "应只授予 ADMIN 角色");
        assertTrue(sql.contains("(m.perm_code LIKE '" + spec.module() + ":%' OR m.path = '/biz')"),
                "应按模块权限前缀和根目录路径选取菜单");
        assertTrue(sql.contains("SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.id AND rm.menu_id = m.id"),
                "授权应通过 NOT EXISTS 保持幂等");
        assertTrue(sql.trim().endsWith(");"), "授权语句应位于脚本末尾");
    }
}
