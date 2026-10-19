package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DdlGeneratorMutaGate1Test {

    @Test
    void tableCommentSingleQuoteIsEscaped() throws Exception {
        ModuleSpec base = SpecLoader.load(Path.of("examples/project.yaml"));
        ModuleSpec spec = new ModuleSpec(
                base.basePackage(),
                base.module(),
                base.entity(),
                base.table(),
                "it's a table",
                base.apiPrefix(),
                base.layout(),
                base.layoutOrigin(),
                base.fields());

        String ddl = DdlGenerator.generate(spec);

        assertThat(ddl).contains("COMMENT='it''s a table';");
    }
}
