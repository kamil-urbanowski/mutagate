package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldSpecMutaGate4Test {

    private static FieldSpec spec(FieldType type, Integer length, Integer precision, Integer scale) {
        return new FieldSpec("f", "f", type, length, precision, scale, true, "c",
                FieldSpec.QueryMode.NONE, false);
    }

    @Test
    void sqlColumnTypeDefaultsAndPlainTypes() {
        // STRING: explicit length and default 255
        assertEquals("VARCHAR(64)", spec(FieldType.STRING, 64, null, null).sqlColumnType());
        assertEquals("VARCHAR(255)", spec(FieldType.STRING, null, null, null).sqlColumnType());
        // precision/scale are ignored for STRING
        assertEquals("VARCHAR(10)", spec(FieldType.STRING, 10, 5, 3).sqlColumnType());

        // DECIMAL: defaults, partial defaults, explicit values
        assertEquals("DECIMAL(18,2)", spec(FieldType.DECIMAL, null, null, null).sqlColumnType());
        assertEquals("DECIMAL(10,2)", spec(FieldType.DECIMAL, null, 10, null).sqlColumnType());
        assertEquals("DECIMAL(18,4)", spec(FieldType.DECIMAL, null, null, 4).sqlColumnType());
        assertEquals("DECIMAL(12,0)", spec(FieldType.DECIMAL, null, 12, 0).sqlColumnType());
        // length is ignored for DECIMAL
        assertEquals("DECIMAL(18,2)", spec(FieldType.DECIMAL, 99, null, null).sqlColumnType());

        // Other types: no parameters, even if length/precision supplied
        assertEquals("TEXT", spec(FieldType.TEXT, 100, 5, 2).sqlColumnType());
        assertEquals("INT", spec(FieldType.INT, 11, null, null).sqlColumnType());
        assertEquals("BIGINT", spec(FieldType.LONG, null, 20, 0).sqlColumnType());
        assertEquals("TINYINT", spec(FieldType.FLAG, null, null, null).sqlColumnType());
        assertEquals("DATE", spec(FieldType.DATE, null, null, null).sqlColumnType());
        assertEquals("DATETIME", spec(FieldType.DATETIME, 6, null, null).sqlColumnType());
    }
}
