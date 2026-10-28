package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldSpecMutaGate2Test {

    private static FieldSpec spec(FieldType type, Integer length, Integer precision, Integer scale) {
        return new FieldSpec("f", "f", type, length, precision, scale, true, "c",
                FieldSpec.QueryMode.NONE, false);
    }

    @Test
    void sqlColumnTypeDefaultsAndPlainTypes() {
        // STRING: explicit length, and default 255 when null
        assertEquals("VARCHAR(64)", spec(FieldType.STRING, 64, null, null).sqlColumnType());
        assertEquals("VARCHAR(255)", spec(FieldType.STRING, null, null, null).sqlColumnType());
        // length is ignored by precision handling; precision/scale ignored for STRING
        assertEquals("VARCHAR(10)", spec(FieldType.STRING, 10, 5, 3).sqlColumnType());

        // DECIMAL: defaults 18,2 individually
        assertEquals("DECIMAL(18,2)", spec(FieldType.DECIMAL, null, null, null).sqlColumnType());
        assertEquals("DECIMAL(10,2)", spec(FieldType.DECIMAL, null, 10, null).sqlColumnType());
        assertEquals("DECIMAL(18,4)", spec(FieldType.DECIMAL, null, null, 4).sqlColumnType());
        assertEquals("DECIMAL(12,3)", spec(FieldType.DECIMAL, 99, 12, 3).sqlColumnType());
        assertEquals("DECIMAL(8,0)", spec(FieldType.DECIMAL, null, 8, 0).sqlColumnType());

        // Types without length/precision ignore those values
        assertEquals("TEXT", spec(FieldType.TEXT, 100, 5, 2).sqlColumnType());
        assertEquals("INT", spec(FieldType.INT, 11, 5, 2).sqlColumnType());
        assertEquals("BIGINT", spec(FieldType.LONG, null, null, null).sqlColumnType());
        assertEquals("TINYINT", spec(FieldType.FLAG, 1, null, null).sqlColumnType());
        assertEquals("DATE", spec(FieldType.DATE, null, null, null).sqlColumnType());
        assertEquals("DATETIME", spec(FieldType.DATETIME, 6, 6, 6).sqlColumnType());
    }
}
