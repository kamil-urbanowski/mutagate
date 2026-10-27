package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DdlGeneratorMutaGate2Test {

    @Test
    void tableCommentSingleQuoteIsEscaped() throws Exception {
        ModuleSpec base = SpecLoader.load(Path.of("examples/project.yaml"));
        ModuleSpec spec = new ModuleSpec(
                base.basePackage(),
                base.module(),
                base.entity(),
                base.table(),
                "Bob's project",
                base.apiPrefix(),
                base.layout(),
                base.layoutOrigin(),
                List.of());

        String ddl = DdlGenerator.generate(spec);

        assertTrue(ddl.contains("COMMENT='Bob''s project';"), ddl);
    }
}
