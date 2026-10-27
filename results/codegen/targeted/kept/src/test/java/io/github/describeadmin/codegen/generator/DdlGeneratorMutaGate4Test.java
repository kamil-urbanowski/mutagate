package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DdlGeneratorMutaGate4Test {

    @Test
    void columnTypeIsPaddedToElevenCharacters() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        assertFalse(spec.fields().isEmpty());

        String ddl = DdlGenerator.generate(spec);

        for (FieldSpec f : spec.fields()) {
            String expected = String.format("%-14s", f.column())
                    + " " + String.format("%-11s", f.sqlColumnType())
                    + " " + (f.nullable() ? "    NULL" : "NOT NULL")
                    + " COMMENT '";
            assertTrue(ddl.contains(expected),
                    "missing padded column definition: [" + expected + "]\n" + ddl);
        }
    }
}
