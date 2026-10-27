package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldSpecMutaGate2Test {

    private static FieldSpec decimal(Integer precision, Integer scale) {
        return new FieldSpec("amount", "amount", FieldType.DECIMAL, null, precision, scale,
                true, "金额", FieldSpec.QueryMode.NONE, false);
    }

    @Test
    void sqlColumnTypeUsesPrecisionWhenGivenAndDefaultWhenNull() {
        // precision null -> default 18
        assertEquals("DECIMAL(18,2)", decimal(null, null).sqlColumnType());
        // precision given -> used as is
        assertEquals("DECIMAL(10,2)", decimal(10, null).sqlColumnType());
        assertEquals("DECIMAL(10,4)", decimal(10, 4).sqlColumnType());
        // precision null, scale given
        assertEquals("DECIMAL(18,5)", decimal(null, 5).sqlColumnType());
    }
}
