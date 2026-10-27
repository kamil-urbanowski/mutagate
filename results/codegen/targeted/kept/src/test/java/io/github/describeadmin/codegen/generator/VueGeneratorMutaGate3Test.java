package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.FieldSpec;
import io.github.describeadmin.codegen.model.ModuleSpec;
import io.github.describeadmin.codegen.parser.SpecLoader;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VueGeneratorMutaGate3Test {

    @Test
    void formModelInitialValuesMatchFieldTypes() throws Exception {
        ModuleSpec spec = SpecLoader.load(Path.of("examples/project.yaml"));
        String page = VueGenerator.page(spec);

        String startMarker = "const form = reactive({\n";
        int start = page.indexOf(startMarker);
        assertTrue(start >= 0, "form block should exist");
        start += startMarker.length();
        int end = page.indexOf("\n});", start);
        assertTrue(end > start, "form block should end");
        String actual = page.substring(start, end);

        String expected = spec.fields().stream()
                .map(f -> "  " + f.name() + ": " + expectedInitial(f) + ",")
                .collect(Collectors.joining("\n"));

        assertEquals(expected, actual);
    }

    private static String expectedInitial(FieldSpec f) {
        switch (f.type()) {
            case INT:
            case DECIMAL:
                return "undefined as number | undefined";
            case FLAG:
                return "1";
            default:
                return "''";
        }
    }
}
