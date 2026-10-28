package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class LayoutMutaGate1Test {

    @Test
    void ofHandlesNullCaseWhitespaceAndInvalidInputs() {
        assertEquals(Optional.empty(), Layout.of(null));
        assertEquals(Optional.empty(), Layout.of(""));
        assertEquals(Optional.empty(), Layout.of("   "));
        assertEquals(Optional.empty(), Layout.of("nest"));
        assertEquals(Optional.empty(), Layout.of("nested flat"));
        assertEquals(Optional.empty(), Layout.of("ne sted"));

        assertSame(Layout.NESTED, Layout.of("nested").orElseThrow());
        assertSame(Layout.NESTED, Layout.of("NESTED").orElseThrow());
        assertSame(Layout.NESTED, Layout.of("  NeStEd\t").orElseThrow());
        assertSame(Layout.NESTED, Layout.of("\nnested\n").orElseThrow());

        assertSame(Layout.FLAT, Layout.of("flat").orElseThrow());
        assertSame(Layout.FLAT, Layout.of("FLAT").orElseThrow());
        assertSame(Layout.FLAT, Layout.of("  fLaT ").orElseThrow());
        assertSame(Layout.FLAT, Layout.of("\tflat\r\n").orElseThrow());

        for (Layout l : Layout.values()) {
            assertSame(l, Layout.of(l.key()).orElseThrow());
            assertSame(l, Layout.of(l.name()).orElseThrow());
        }
    }
}
