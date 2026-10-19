package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldSpecMutaGate1Test {

    private static FieldSpec stringField(Integer length) {
        return new FieldSpec("name", "name", FieldType.STRING, length, null, null,
                true, "comment", FieldSpec.QueryMode.NONE, false);
    }

    @Test
    void sqlColumnTypeUsesGivenLengthOrDefaultsTo255() {
        assertEquals("VARCHAR(128)", stringField(128).sqlColumnType());
        assertEquals("VARCHAR(255)", stringField(null).sqlColumnType());
    }
}
