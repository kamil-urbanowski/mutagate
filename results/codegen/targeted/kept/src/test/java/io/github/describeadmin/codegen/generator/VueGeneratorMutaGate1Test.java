package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.FieldType;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate1Test {

    @Test
    void decimalPrecisionUsesScaleOrDefaultsToTwo() throws Exception {
        ModuleSpec base = SpecLoader.load(Path.of("examples/project.yaml"));

        FieldSpec withScale = new FieldSpec(
                "amountA", "amount_a", FieldType.DECIMAL, null, 10, 4,
                true, "金额A", FieldSpec.QueryMode.NONE, false);
        FieldSpec withoutScale = new FieldSpec(
                "amountB", "amount_b", FieldType.DECIMAL, null, 10, null,
                true, "金额B", FieldSpec.QueryMode.NONE, false);

        ModuleSpec spec = new ModuleSpec(
                base.basePackage(), base.module(), base.entity(), base.table(),
                base.comment(), base.apiPrefix(), base.layout(), base.layoutOrigin(),
                List.of(withScale, withoutScale));

        String page = VueGenerator.page(spec);

        int idxA = page.indexOf("data-testid=\"" + spec.module() + "-amount-a-input\"");
        int idxB = page.indexOf("data-testid=\"" + spec.module() + "-amount-b-input\"");
        assertTrue(idxA > 0);
        assertTrue(idxB > 0);

        String beforeA = page.substring(Math.max(0, idxA - 120), idxA);
        String beforeB = page.substring(Math.max(0, idxB - 120), idxB);

        assertTrue(beforeA.contains(":precision=\"4\""), beforeA);
        assertTrue(beforeB.contains(":precision=\"2\""), beforeB);
        assertFalse(page.contains(":precision=\"null\""));
    }
}
