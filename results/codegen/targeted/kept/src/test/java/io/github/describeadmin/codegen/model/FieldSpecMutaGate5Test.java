package io.github.describeadmin.codegen.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FieldSpecMutaGate5Test {

    @Test
    void sqlColumnTypeForPlainTypesReturnsBareSqlType() {
        FieldSpec intField = new FieldSpec("age", "age", FieldType.INT, null, null, null,
                true, "age", FieldSpec.QueryMode.NONE, false);
        FieldSpec dateTimeField = new FieldSpec("createdAt", "created_at", FieldType.DATETIME,
                null, null, null, true, "created", FieldSpec.QueryMode.NONE, false);

        assertEquals("INT", intField.sqlColumnType());
        assertEquals("DATETIME", dateTimeField.sqlColumnType());
    }
}
