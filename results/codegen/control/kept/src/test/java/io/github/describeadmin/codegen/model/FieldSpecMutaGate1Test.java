package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldSpecMutaGate1Test {

    private static FieldSpec spec(FieldType type, Integer length, Integer precision, Integer scale) {
        return new FieldSpec("f", "f", type, length, precision, scale, true, "c",
                FieldSpec.QueryMode.NONE, false);
    }

    @Test
    void sqlColumnTypeDefaultsAndPlainTypes() {
        // STRING: default length 255 when null, explicit length otherwise
        assertEquals("VARCHAR(255)", spec(FieldType.STRING, null, null, null).sqlColumnType());
        assertEquals("VARCHAR(64)", spec(FieldType.STRING, 64, 10, 5).sqlColumnType());

        // DECIMAL: default precision 18 and scale 2 independently
        assertEquals("DECIMAL(18,2)", spec(FieldType.DECIMAL, 99, null, null).sqlColumnType());
        assertEquals("DECIMAL(10,2)", spec(FieldType.DECIMAL, null, 10, null).sqlColumnType());
        assertEquals("DECIMAL(18,4)", spec(FieldType.DECIMAL, null, null, 4).sqlColumnType());
        assertEquals("DECIMAL(12,0)", spec(FieldType.DECIMAL, null, 12, 0).sqlColumnType());

        // Types needing neither length nor precision ignore those values
        assertEquals("TEXT", spec(FieldType.TEXT, 100, 10, 2).sqlColumnType());
        assertEquals("INT", spec(FieldType.INT, 11, 10, 2).sqlColumnType());
        assertEquals("BIGINT", spec(FieldType.LONG, null, null, null).sqlColumnType());
        assertEquals("TINYINT", spec(FieldType.FLAG, 1, null, null).sqlColumnType());
        assertEquals("DATE", spec(FieldType.DATE, null, null, null).sqlColumnType());
        assertEquals("DATETIME", spec(FieldType.DATETIME, 6, 6, 6).sqlColumnType());
    }
}
