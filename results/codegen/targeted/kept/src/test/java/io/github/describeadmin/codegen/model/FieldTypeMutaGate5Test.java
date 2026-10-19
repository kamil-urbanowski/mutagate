package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldTypeMutaGate5Test {

    @Test
    void needsPrecisionOnlyForDecimal() {
        assertTrue(FieldType.DECIMAL.needsPrecision());
        for (FieldType type : FieldType.values()) {
            if (type != FieldType.DECIMAL) {
                assertFalse(type.needsPrecision(), type + " should not need precision");
            }
        }
        assertEquals(false, FieldType.STRING.needsPrecision());
    }
}
