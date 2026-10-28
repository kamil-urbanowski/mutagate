package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuSqlGeneratorMutaGate1Test {

    @Test
    void generatesSectionsInOrderWithModuleSpecificValues() throws Exception {
        ModuleSpec base = SpecLoader.load(Path.of("examples/project.yaml"));
        ModuleSpec s = new ModuleSpec(base.basePackage(), "order", base.entity(), base.table(),
                "订单", "/api/order-items", base.layout(), base.layoutOrigin(), base.fields());

        String sql = MenuSqlGenerator.generate(s);

        // header
        assertTrue(sql.startsWith("-- ====="), "should start with header comment");
        assertTrue(sql.contains("-- 订单 菜单与权限点"));
        assertTrue(sql.contains("'order/index' 对应 frontend 的 src/views/order/index.vue"));

        // list menu row: /api prefix stripped, comment used as name
        assertTrue(sql.contains(
                "SELECT m.id, '订单', 'MENU', 'order:list', '/order-items', 'order/index', 'lucide:table', 1, 1,"));
        assertFalse(sql.contains("'/api/order-items'"));

        // button rows with their sort values
        assertTrue(sql.contains(
                "SELECT m.id, '新增', 'BUTTON', 'order:add', NULL, NULL, NULL, 1, 1, NOW(), NOW(), 0, 0"));
        assertTrue(sql.contains(
                "SELECT m.id, '编辑', 'BUTTON', 'order:edit', NULL, NULL, NULL, 2, 1, NOW(), NOW(), 0, 0"));
        assertTrue(sql.contains(
                "SELECT m.id, '删除', 'BUTTON', 'order:remove', NULL, NULL, NULL, 3, 1, NOW(), NOW(), 0, 0"));

        // grant uses a single % wildcard and the root path
        assertTrue(sql.contains("m.perm_code LIKE 'order:%' OR m.path = '/biz'"));
        assertFalse(sql.contains("'order:%%'"));

        // overall ordering of the sections
        int root = sql.indexOf("-- 业务菜单根目录");
        int list = sql.indexOf("-- 列表页");
        int add = sql.indexOf("'order:add'");
        int edit = sql.indexOf("'order:edit'");
        int remove = sql.indexOf("'order:remove'");
        int grant = sql.indexOf("-- 授予 ADMIN");
        assertTrue(root > 0);
        assertTrue(root < list);
        assertTrue(list < add);
        assertTrue(add < edit);
        assertTrue(edit < remove);
        assertTrue(remove < grant);

        // 1 root dir + 1 list + 3 buttons + 1 grant into sys_role_menu
        assertEquals(5, countOccurrences(sql, "INSERT INTO sys_menu"));
        assertEquals(1, countOccurrences(sql, "INSERT INTO sys_role_menu"));
    }

    private static int countOccurrences(String text, String needle) {
        int count = 0;
        int from = 0;
        while ((from = text.indexOf(needle, from)) >= 0) {
            count++;
            from += needle.length();
        }
        return count;
    }
}
