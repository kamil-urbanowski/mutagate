package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldTypeMutaGate4Test {

    @Test
    void needsLengthIsTrueOnlyForString() {
        for (FieldType type : FieldType.values()) {
            if (type == FieldType.STRING) {
                assertTrue(type.needsLength(), "STRING should need length");
            } else {
                assertFalse(type.needsLength(), type + " should not need length");
            }
        }
    }
}
