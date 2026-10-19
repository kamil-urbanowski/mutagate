package io.github.describeadmin.codegen.generator;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

class DdlGeneratorMutaGate4Test {

    @Test
    void columnTypeIsPaddedToElevenCharacters() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String ddl = DdlGenerator.generate(spec);

        assertTrue(!spec.fields().isEmpty());
        for (FieldSpec f : spec.fields()) {
            String comment = f.comment() == null ? "" : f.comment().replace("'", "''");
            String expected = String.format("%-14s", f.column())
                    + " " + String.format("%-11s", f.sqlColumnType())
                    + " " + (f.nullable() ? "    NULL" : "NOT NULL")
                    + " COMMENT '" + comment + "'";
            assertTrue(ddl.contains(expected), "missing column line: " + expected);
        }
    }
}
