package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DdlGeneratorMutaGate3Test {

    @Test
    void nullabilityOfEachBusinessColumnMatchesSpec() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String ddl = DdlGenerator.generate(spec);
        String[] lines = ddl.split("\n");

        assertThat(spec.fields()).isNotEmpty();

        for (FieldSpec f : spec.fields()) {
            String prefix = String.format("%-14s", f.column()) + " ";
            String found = null;
            for (String line : lines) {
                if (line.stripLeading().startsWith(prefix)) {
                    found = line;
                    break;
                }
            }
            assertThat(found).as("column line for %s", f.column()).isNotNull();
            if (f.nullable()) {
                assertThat(found).as(f.column()).contains("    NULL COMMENT '");
                assertThat(found).as(f.column()).doesNotContain("NOT NULL");
            } else {
                assertThat(found).as(f.column()).contains("NOT NULL COMMENT '");
            }
        }
    }
}
