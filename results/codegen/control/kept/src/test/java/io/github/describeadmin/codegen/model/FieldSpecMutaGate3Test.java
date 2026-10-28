package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldSpecMutaGate3Test {

    private static FieldSpec spec(FieldType type, Integer length, Integer precision, Integer scale) {
        return new FieldSpec("f", "f", type, length, precision, scale, true, "c",
                FieldSpec.QueryMode.NONE, false);
    }

    @Test
    void sqlColumnTypeDefaultsAndNonParameterizedTypes() {
        // STRING: explicit length and default 255
        assertEquals("VARCHAR(64)", spec(FieldType.STRING, 64, null, null).sqlColumnType());
        assertEquals("VARCHAR(255)", spec(FieldType.STRING, null, null, null).sqlColumnType());
        // length is ignored for precision-based types and vice versa
        assertEquals("VARCHAR(10)", spec(FieldType.STRING, 10, 30, 5).sqlColumnType());

        // DECIMAL: defaults, partial defaults, explicit values
        assertEquals("DECIMAL(18,2)", spec(FieldType.DECIMAL, null, null, null).sqlColumnType());
        assertEquals("DECIMAL(10,2)", spec(FieldType.DECIMAL, null, 10, null).sqlColumnType());
        assertEquals("DECIMAL(18,4)", spec(FieldType.DECIMAL, null, null, 4).sqlColumnType());
        assertEquals("DECIMAL(12,0)", spec(FieldType.DECIMAL, 99, 12, 0).sqlColumnType());

        // Types needing neither length nor precision ignore those values
        assertEquals("TEXT", spec(FieldType.TEXT, 100, 10, 2).sqlColumnType());
        assertEquals("INT", spec(FieldType.INT, 11, null, null).sqlColumnType());
        assertEquals("BIGINT", spec(FieldType.LONG, null, 20, 0).sqlColumnType());
        assertEquals("TINYINT", spec(FieldType.FLAG, 1, null, null).sqlColumnType());
        assertEquals("DATE", spec(FieldType.DATE, null, null, null).sqlColumnType());
        assertEquals("DATETIME", spec(FieldType.DATETIME, null, null, null).sqlColumnType());
    }
}
