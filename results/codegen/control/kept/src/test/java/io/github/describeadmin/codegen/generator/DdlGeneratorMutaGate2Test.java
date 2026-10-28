package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DdlGeneratorMutaGate2Test {

    @Test
    void escapesQuotesAndRendersNullabilityIndexAndOrdering() throws Exception {
        ModuleSpec base = SpecLoader.load(Path.of("examples/project.yaml"));
        FieldSpec f = base.fields().get(0);

        FieldSpec notNullIndexed = new FieldSpec(f.name(), "first_col", f.type(), f.length(),
                f.precision(), f.scale(), false, "it's", f.query(), true);
        FieldSpec nullablePlain = new FieldSpec(f.name(), "other_col", f.type(), f.length(),
                f.precision(), f.scale(), true, "plain", f.query(), false);

        ModuleSpec s = new ModuleSpec(base.basePackage(), base.module(), base.entity(),
                base.table(), "O'Brien's", base.apiPrefix(), base.layout(),
                base.layoutOrigin(), List.of(notNullIndexed, nullablePlain));

        String ddl = DdlGenerator.generate(s);

        // header comment is raw, table comment is escaped
        assertThat(ddl).startsWith("-- O'Brien's\n--\n");
        assertThat(ddl).contains("COMMENT='O''Brien''s';");

        // column comment escaped, nullability rendered
        assertThat(ddl).contains("NOT NULL COMMENT 'it''s'");
        assertThat(ddl).contains("    NULL COMMENT 'plain'");

        // only the indexed field gets a secondary key
        String idx = "KEY idx_" + s.table() + "_first_col (first_col)";
        assertThat(ddl).contains(idx);
        assertThat(ddl).doesNotContain("idx_" + s.table() + "_other_col");
        assertThat(ddl.split("KEY idx_", -1)).hasSize(2);

        // secondary keys come last, directly before the table options
        assertThat(ddl).contains(idx + "\n) ENGINE=InnoDB\n  DEFAULT CHARACTER SET utf8mb4");

        // ordering: id, business columns, audit columns, primary key, secondary key
        int id = ddl.indexOf("id ");
        int first = ddl.indexOf("first_col ");
        int other = ddl.indexOf("other_col ");
        int createBy = ddl.indexOf("create_by");
        int createTime = ddl.indexOf("create_time");
        int updateBy = ddl.indexOf("update_by");
        int updateTime = ddl.indexOf("update_time");
        int deleted = ddl.indexOf("deleted ");
        int version = ddl.indexOf("version ");
        int pk = ddl.indexOf("PRIMARY KEY (id)");
        int key = ddl.indexOf(idx);
        assertThat(List.of(id, first, other, createBy, createTime, updateBy, updateTime,
                deleted, version, pk, key)).isSorted();
        assertThat(id).isGreaterThanOrEqualTo(0);

        // comma separators between items
        assertThat(ddl).contains(",\n  PRIMARY KEY (id),\n  " + idx);
    }
}
