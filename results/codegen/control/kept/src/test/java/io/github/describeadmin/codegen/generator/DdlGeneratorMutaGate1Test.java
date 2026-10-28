package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DdlGeneratorMutaGate1Test {

    @Test
    void generatesExactColumnLinesEscapingOrderingAndKeys() throws Exception {
        ModuleSpec loaded = SpecLoader.load(Path.of("examples/project.yaml"));
        FieldSpec base = loaded.fields().get(0);

        FieldSpec a = new FieldSpec("alpha", "col_a", base.type(), base.length(), base.precision(),
                base.scale(), false, "it's a", base.query(), true);
        FieldSpec b = new FieldSpec("beta", "col_b", base.type(), base.length(), base.precision(),
                base.scale(), true, "plain", base.query(), false);

        ModuleSpec spec = new ModuleSpec(loaded.basePackage(), loaded.module(), loaded.entity(),
                "t_demo", "it's table", loaded.apiPrefix(), loaded.layout(), loaded.layoutOrigin(),
                List.of(a, b));

        String ddl = DdlGenerator.generate(spec);

        String type = String.format("%-11s", a.sqlColumnType());
        String lineA = String.format("%-14s", "col_a") + " " + type + " NOT NULL COMMENT 'it''s a'";
        String lineB = String.format("%-14s", "col_b") + " "
                + String.format("%-11s", b.sqlColumnType()) + "     NULL COMMENT 'plain'";

        // header comment is unescaped, table comment clause is escaped
        assertThat(ddl).startsWith("-- it's table\n--\n");
        assertThat(ddl).contains("CREATE TABLE IF NOT EXISTS t_demo (\n  ");
        assertThat(ddl).contains("COMMENT='it''s table';");

        assertThat(ddl).contains(lineA).contains(lineB);

        // fixed ordering: id, business fields, audit fields, primary key, secondary keys
        int idPos = ddl.indexOf("COMMENT '主键'");
        int aPos = ddl.indexOf(lineA);
        int bPos = ddl.indexOf(lineB);
        int auditPos = ddl.indexOf("COMMENT '创建人'");
        int pkPos = ddl.indexOf("PRIMARY KEY (id)");
        assertThat(idPos).isPositive();
        assertThat(aPos).isGreaterThan(idPos);
        assertThat(bPos).isGreaterThan(aPos);
        assertThat(auditPos).isGreaterThan(bPos);
        assertThat(pkPos).isGreaterThan(auditPos);

        // only the indexed field gets a key; keys are last, joined by ",\n  "
        assertThat(ddl).contains("COMMENT '乐观锁版本号',\n  PRIMARY KEY (id),\n"
                + "  KEY idx_t_demo_col_a (col_a)\n) ENGINE=InnoDB\n"
                + "  DEFAULT CHARACTER SET utf8mb4\n  COLLATE utf8mb4_general_ci\n");
        assertThat(ddl).doesNotContain("idx_t_demo_col_b");
        assertThat(ddl).contains(lineA + ",\n  " + lineB + ",\n  ");
    }
}
