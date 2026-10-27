package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldSpecMutaGate5Test {

    @Test
    void sqlColumnTypeForTypesWithoutLengthOrPrecisionIsPlainSqlType() {
        FieldSpec intSpec = new FieldSpec("count", "count", FieldType.INT, null, null, null,
                true, "数量", FieldSpec.QueryMode.NONE, false);
        FieldSpec dateTimeSpec = new FieldSpec("createdAt", "created_at", FieldType.DATETIME,
                null, null, null, true, "时间", FieldSpec.QueryMode.NONE, false);

        assertEquals("INT", intSpec.sqlColumnType());
        assertEquals("DATETIME", dateTimeSpec.sqlColumnType());
    }
}
