package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldTypeMutaGate5Test {

    @Test
    void needsPrecisionOnlyForDecimal() {
        for (FieldType type : FieldType.values()) {
            assertEquals(type == FieldType.DECIMAL, type.needsPrecision(),
                    "needsPrecision mismatch for " + type);
        }
    }
}
