package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.Layout;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate1Test {

    @Test
    void invalidLayoutRecordsErrorAndReturnsNestedPlaceholder() {
        List<String> errors = new ArrayList<>();

        SpecLoader.LayoutChoice choice = SpecLoader.resolveLayout("bogus", null, null, errors);

        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("bogus"));
        assertEquals(Layout.NESTED, choice.layout());
        assertEquals("--layout 参数", choice.origin());
    }
}
