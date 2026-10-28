package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DdlGeneratorMutaGate4Test {

    @Test
    void escapesQuotesAndKeepsColumnLayoutAndOrder() throws Exception {
        ModuleSpec base = SpecLoader.load(Path.of("examples/project.yaml"));

        List<FieldSpec> fields = new ArrayList<>(base.fields());
        FieldSpec orig = fields.get(0);
        FieldSpec quoted = new FieldSpec(orig.name(), orig.column(), orig.type(), orig.length(),
                orig.precision(), orig.scale(), orig.nullable(), "it's", orig.query(),
                orig.indexed());
        fields.set(0, quoted);

        ModuleSpec s = new ModuleSpec(base.basePackage(), base.module(), base.entity(),
                base.table(), "O'Brien", base.apiPrefix(), base.layout(), base.layoutOrigin(),
                fields);

        String ddl = DdlGenerator.generate(s);

        // header comment is raw, table comment is escaped
        assertTrue(ddl.startsWith("-- O'Brien\n--\n"));
        assertTrue(ddl.contains("COMMENT='O''Brien';"));
        assertTrue(ddl.endsWith(";\n"));
        assertTrue(ddl.contains("CREATE TABLE IF NOT EXISTS " + base.table() + " (\n  id "));

        // exact id line and field line formatting
        String idLine = String.format("%-14s", "id") + " BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键'";
        String fieldLine = String.format("%-14s", quoted.column()) + " "
                + String.format("%-11s", quoted.sqlColumnType()) + " "
                + (quoted.nullable() ? "    NULL" : "NOT NULL")
                + " COMMENT 'it''s'";
        assertTrue(ddl.contains(idLine + ",\n  " + fieldLine + ",\n  "));

        // order: id, fields, audit columns, primary key, then indexes
        int idPos = ddl.indexOf(idLine);
        int fieldPos = ddl.indexOf(fieldLine);
        int createBy = ddl.indexOf(String.format("%-14s", "create_by"));
        int version = ddl.indexOf(String.format("%-14s", "version"));
        int pk = ddl.indexOf("PRIMARY KEY (id)");
        assertTrue(idPos < fieldPos);
        assertTrue(fieldPos < createBy);
        assertTrue(createBy < version);
        assertTrue(version < pk);

        // primary key line is followed by index lines only; no trailing comma before ")"
        assertTrue(ddl.contains("\n) ENGINE=InnoDB\n"));
        assertEquals(-1, ddl.indexOf(",\n)"));
    }
}
