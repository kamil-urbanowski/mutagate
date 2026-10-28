package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.FieldType;
import io.github.describeadmin.codegen.model.Layout;
import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DdlGeneratorMutaGate3Test {

    private static FieldSpec field(String name, String column, boolean nullable,
                                   String comment, boolean indexed) {
        return new FieldSpec(name, column, FieldType.values()[0], 10, 10, 2,
                nullable, comment, FieldSpec.QueryMode.NONE, indexed);
    }

    private static String lineStarting(String ddl, String prefix) {
        for (String line : ddl.split("\n")) {
            if (line.strip().startsWith(prefix)) {
                return line;
            }
        }
        throw new AssertionError("no line starting with " + prefix + " in:\n" + ddl);
    }

    @Test
    void generatesOrderedColumnsEscapedCommentsAndKeys() {
        ModuleSpec spec = new ModuleSpec("com.example", "demo", "Demo", "t_demo",
                "Bob's table", "/api/demo", Layout.values()[0], "test",
                List.of(
                        field("a", "col_a", false, "it's a", true),
                        field("b", "col_b", true, null, false),
                        field("c", "col_c", false, "plain", true)));

        String ddl = DdlGenerator.generate(spec);

        // header comment is raw, table COMMENT is escaped
        assertTrue(ddl.startsWith("-- Bob's table\n"));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS t_demo ("));
        assertTrue(ddl.contains("COMMENT='Bob''s table';"));

        // column comment escaping and null comment handling
        String a = lineStarting(ddl, "col_a");
        assertTrue(a.contains("NOT NULL"));
        assertTrue(a.endsWith("COMMENT 'it''s a',"));
        String b = lineStarting(ddl, "col_b");
        assertFalse(b.contains("NOT NULL"));
        assertTrue(b.contains("    NULL"));
        assertTrue(b.endsWith("COMMENT '',"));

        // ordering: id, business columns, audit columns, primary key, indexes
        int id = ddl.indexOf("id            ");
        int ca = ddl.indexOf("col_a");
        int cb = ddl.indexOf("col_b");
        int cc = ddl.indexOf("col_c");
        int createBy = ddl.indexOf("create_by");
        int createTime = ddl.indexOf("create_time");
        int updateBy = ddl.indexOf("update_by");
        int updateTime = ddl.indexOf("update_time");
        int deleted = ddl.indexOf("deleted");
        int version = ddl.indexOf("version");
        int pk = ddl.indexOf("PRIMARY KEY (id)");
        int ka = ddl.indexOf("KEY idx_t_demo_col_a (col_a)");
        int kc = ddl.indexOf("KEY idx_t_demo_col_c (col_c)");
        assertTrue(id >= 0 && id < ca);
        assertTrue(ca < cb && cb < cc && cc < createBy);
        assertTrue(createBy < createTime && createTime < updateBy
                && updateBy < updateTime && updateTime < deleted && deleted < version);
        assertTrue(version < pk && pk < ka && ka < kc);
        assertFalse(ddl.contains("idx_t_demo_col_b"));

        // comma separation: last column and primary key end with commas, last key does not
        assertTrue(lineStarting(ddl, "version").endsWith(","));
        assertTrue(lineStarting(ddl, "PRIMARY KEY").endsWith(","));
        assertTrue(lineStarting(ddl, "KEY idx_t_demo_col_a").endsWith(","));
        assertEquals("  KEY idx_t_demo_col_c (col_c)", lineStarting(ddl, "KEY idx_t_demo_col_c"));
        assertTrue(ddl.contains("KEY idx_t_demo_col_c (col_c)\n) ENGINE=InnoDB"));
    }
}
