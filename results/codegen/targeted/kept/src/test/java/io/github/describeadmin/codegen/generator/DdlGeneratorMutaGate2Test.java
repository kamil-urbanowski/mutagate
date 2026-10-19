package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DdlGeneratorMutaGate2Test {

    @Test
    void tableCommentSingleQuoteIsEscaped() throws Exception {
        ModuleSpec base = SpecLoader.load(Path.of("examples/project.yaml"));
        ModuleSpec s = new ModuleSpec(
                base.basePackage(),
                base.module(),
                base.entity(),
                base.table(),
                "it's a table",
                base.apiPrefix(),
                base.layout(),
                base.layoutOrigin(),
                base.fields());

        String ddl = DdlGenerator.generate(s);

        assertTrue(ddl.contains("COMMENT='it''s a table';"), ddl);
        assertTrue(ddl.contains("-- it's a table"), ddl);
    }
}
