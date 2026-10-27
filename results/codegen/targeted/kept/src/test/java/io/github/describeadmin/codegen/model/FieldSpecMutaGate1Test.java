package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldSpecMutaGate1Test {

    private static FieldSpec string(Integer length) {
        return new FieldSpec("projectName", "project_name", FieldType.STRING,
                length, null, null, true, "name", FieldSpec.QueryMode.NONE, false);
    }

    @Test
    void sqlColumnTypeUsesGivenLengthOrDefault() {
        assertEquals("VARCHAR(128)", string(128).sqlColumnType());
        assertEquals("VARCHAR(255)", string(null).sqlColumnType());
    }
}
