package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DdlGeneratorMutaGate3Test {

    @Test
    void nullabilityOfEachFieldIsReflectedInColumnDefinition() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String ddl = DdlGenerator.generate(spec);

        assertThat(spec.fields()).isNotEmpty();

        for (FieldSpec f : spec.fields()) {
            String line = null;
            for (String l : ddl.split("\n")) {
                if (l.strip().startsWith(f.column() + " ") && l.contains("COMMENT")) {
                    line = l;
                    break;
                }
            }
            assertThat(line).as("column line for %s", f.column()).isNotNull();

            if (f.nullable()) {
                assertThat(line).as(f.column()).contains(" NULL COMMENT");
                assertThat(line).as(f.column()).doesNotContain("NOT NULL");
            } else {
                assertThat(line).as(f.column()).contains("NOT NULL COMMENT");
            }
        }
    }
}
