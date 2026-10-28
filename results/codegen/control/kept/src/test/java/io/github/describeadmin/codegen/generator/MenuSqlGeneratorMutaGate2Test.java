package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MenuSqlGeneratorMutaGate2Test {

    private static int count(String text, String needle) {
        int n = 0;
        int from = 0;
        while ((from = text.indexOf(needle, from)) >= 0) {
            n++;
            from += needle.length();
        }
        return n;
    }

    @Test
    void generatesStatementsInOrderWithExpectedRowsAndRoute() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String sql = MenuSqlGenerator.generate(spec);
        String module = spec.module();
        String route = spec.apiPrefix().replaceFirst("^/api", "");

        // 5 sys_menu inserts (root dir, list page, 3 buttons) and exactly one grant
        assertEquals(5, count(sql, "INSERT INTO sys_menu"));
        assertEquals(1, count(sql, "INSERT INTO sys_role_menu"));

        // header carries the module comment
        assertTrue(sql.startsWith("-- ====="));
        assertTrue(sql.contains("-- " + spec.comment() + " 菜单与权限点"));

        // list page row: name, perm, route without /api prefix, component
        String menuRow = "SELECT m.id, '" + spec.comment() + "', 'MENU', '" + module + ":list', '"
                + route + "', '" + module + "/index'";
        assertTrue(sql.contains(menuRow), "menu row missing");
        assertTrue(!route.startsWith("/api"));

        // button rows with label and sort order
        String add = "SELECT m.id, '新增', 'BUTTON', '" + module + ":add', NULL, NULL, NULL, 1, 1, NOW()";
        String edit = "SELECT m.id, '编辑', 'BUTTON', '" + module + ":edit', NULL, NULL, NULL, 2, 1, NOW()";
        String remove = "SELECT m.id, '删除', 'BUTTON', '" + module + ":remove', NULL, NULL, NULL, 3, 1, NOW()";
        assertTrue(sql.contains(add), "add button missing");
        assertTrue(sql.contains(edit), "edit button missing");
        assertTrue(sql.contains(remove), "remove button missing");

        // buttons hang under the list permission
        assertEquals(3, count(sql, "WHERE m.perm_code = '" + module + ":list' AND m.deleted = 0"));

        // ordering: root dir -> list page -> add -> edit -> remove -> grant
        int dir = sql.indexOf("'DIR'");
        int menu = sql.indexOf(menuRow);
        int iAdd = sql.indexOf(add);
        int iEdit = sql.indexOf(edit);
        int iRemove = sql.indexOf(remove);
        int grant = sql.indexOf("INSERT INTO sys_role_menu");
        assertTrue(dir > 0);
        assertTrue(dir < menu);
        assertTrue(menu < iAdd);
        assertTrue(iAdd < iEdit);
        assertTrue(iEdit < iRemove);
        assertTrue(iRemove < grant);

        // grant is limited to ADMIN and to this module's perms plus the root dir
        String tail = sql.substring(grant);
        assertTrue(tail.contains("r.role_code = 'ADMIN'"));
        assertTrue(tail.contains("m.perm_code LIKE '" + module + ":%' OR m.path = '/biz'"));
        assertTrue(!tail.contains("%%"));
    }
}
