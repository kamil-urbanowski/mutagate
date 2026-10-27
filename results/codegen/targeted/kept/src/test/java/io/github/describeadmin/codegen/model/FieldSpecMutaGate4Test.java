package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldSpecMutaGate4Test {

    @Test
    void sqlColumnTypeForStringUsesLengthOrDefault() {
        FieldSpec withLength = new FieldSpec("projectName", "project_name", FieldType.STRING,
                128, null, null, false, "name", FieldSpec.QueryMode.NONE, false);
        FieldSpec withoutLength = new FieldSpec("projectName", "project_name", FieldType.STRING,
                null, null, null, false, "name", FieldSpec.QueryMode.NONE, false);

        assertEquals("VARCHAR(128)", withLength.sqlColumnType());
        assertEquals("VARCHAR(255)", withoutLength.sqlColumnType());
    }
}
