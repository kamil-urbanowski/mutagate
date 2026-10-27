package io.github.describeadmin.codegen.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FieldSpecMutaGate3Test {

    private static FieldSpec decimal(Integer precision, Integer scale) {
        return new FieldSpec("amount", "amount", FieldType.DECIMAL, null, precision, scale,
                true, "金额", FieldSpec.QueryMode.NONE, false);
    }

    @Test
    void sqlColumnTypeUsesGivenScaleAndDefaultsWhenNull() {
        assertEquals("DECIMAL(10,5)", decimal(10, 5).sqlColumnType());
        assertEquals("DECIMAL(10,2)", decimal(10, null).sqlColumnType());
        assertEquals("DECIMAL(18,3)", decimal(null, 3).sqlColumnType());
        assertEquals("DECIMAL(18,2)", decimal(null, null).sqlColumnType());
    }
}
