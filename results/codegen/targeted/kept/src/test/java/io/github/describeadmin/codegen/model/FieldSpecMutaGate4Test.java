package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldSpecMutaGate4Test {

    @Test
    void sqlColumnTypeForStringUsesLengthOrDefault() {
        FieldSpec withLength = new FieldSpec("name", "name", FieldType.STRING, 128, null, null,
                true, "c", FieldSpec.QueryMode.NONE, false);
        FieldSpec withoutLength = new FieldSpec("name", "name", FieldType.STRING, null, null, null,
                true, "c", FieldSpec.QueryMode.NONE, false);

        assertEquals("VARCHAR(128)", withLength.sqlColumnType());
        assertEquals("VARCHAR(255)", withoutLength.sqlColumnType());
    }
}
