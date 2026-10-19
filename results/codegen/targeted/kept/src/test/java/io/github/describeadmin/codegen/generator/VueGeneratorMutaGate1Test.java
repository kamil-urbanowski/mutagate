package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.FieldType;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class VueGeneratorMutaGate1Test {

    @Test
    void decimalPrecisionUsesExplicitScale() throws Exception {
        ModuleSpec base = SpecLoader.load(Path.of("examples/project.yaml"));

        List<FieldSpec> fields = new ArrayList<>();
        for (FieldSpec f : base.fields()) {
            if (f.type() == FieldType.DECIMAL) {
                fields.add(new FieldSpec(
                        f.name(), f.column(), f.type(), f.length(),
                        10, 3,
                        f.nullable(), f.comment(), f.query(), f.indexed()));
            } else {
                fields.add(f);
            }
        }

        ModuleSpec spec = new ModuleSpec(
                base.basePackage(), base.module(), base.entity(), base.table(),
                base.comment(), base.apiPrefix(), base.layout(), base.layoutOrigin(),
                fields);

        String page = VueGenerator.page(spec);

        assertTrue(page.contains(":precision=\"3\""), "explicit scale must be used");
        assertFalse(page.contains(":precision=\"2\""), "default scale must not be used");
    }
}
