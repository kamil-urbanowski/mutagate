package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LayoutMutaGate1Test {

    @Test
    void keyReturnsLowercaseName() {
        assertEquals("nested", Layout.NESTED.key());
        assertEquals("flat", Layout.FLAT.key());
    }
}
