package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldSpecMutaGate3Test {

    private static FieldSpec decimal(Integer precision, Integer scale) {
        return new FieldSpec("amount", "amount", FieldType.DECIMAL, null, precision, scale,
                true, "金额", FieldSpec.QueryMode.NONE, false);
    }

    @Test
    void sqlColumnTypeUsesExplicitScaleAndDefaultsWhenNull() {
        assertEquals("DECIMAL(10,4)", decimal(10, 4).sqlColumnType());
        assertEquals("DECIMAL(10,0)", decimal(10, 0).sqlColumnType());
        assertEquals("DECIMAL(10,2)", decimal(10, null).sqlColumnType());
        assertEquals("DECIMAL(18,2)", decimal(null, null).sqlColumnType());
    }
}
