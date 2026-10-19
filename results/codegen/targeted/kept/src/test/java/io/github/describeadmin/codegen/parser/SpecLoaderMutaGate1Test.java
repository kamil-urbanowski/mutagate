package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.Layout;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SpecLoaderMutaGate1Test {

    @Test
    void invalidLayoutValueYieldsNestedPlaceholderAndRecordsError() {
        List<String> errors = new ArrayList<>();

        SpecLoader.LayoutChoice choice = SpecLoader.resolveLayout("sideways", null, null, errors);

        assertEquals(Layout.NESTED, choice.layout());
        assertEquals("--layout 参数", choice.origin());
        assertEquals(1, errors.size());
    }
}
