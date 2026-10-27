package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.FieldType;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate8Test {

    private static String ts(FieldType type) {
        return switch (type) {
            case STRING, TEXT, DATE, DATETIME, LONG -> "string";
            case INT, DECIMAL, FLAG -> "number";
        };
    }

    @Test
    void searchModelSplitsOnlyRangeFields() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        List<FieldSpec> queryFields = spec.queryFields();

        assertFalse(queryFields.isEmpty());
        assertTrue(queryFields.stream().anyMatch(f -> f.query() == FieldSpec.QueryMode.RANGE));

        List<String> expectedLines = new ArrayList<>();
        for (FieldSpec f : queryFields) {
            if (f.query() == FieldSpec.QueryMode.RANGE) {
                expectedLines.add("  " + f.name() + "End: undefined as " + ts(f.type()) + " | undefined,");
                expectedLines.add("  " + f.name() + "Start: undefined as " + ts(f.type()) + " | undefined,");
            } else {
                expectedLines.add("  " + f.name() + ": undefined as " + ts(f.type()) + " | undefined,");
            }
        }

        String page = VueGenerator.page(spec);
        String marker = "const search = reactive({\n";
        int start = page.indexOf(marker);
        assertTrue(start >= 0);
        start += marker.length();
        int end = page.indexOf("\n});", start);
        assertTrue(end > start);

        assertEquals(String.join("\n", expectedLines), page.substring(start, end));
    }
}
