package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldTypeMutaGate4Test {

    @Test
    void needsLengthIsTrueOnlyForString() {
        assertTrue(FieldType.STRING.needsLength());
        for (FieldType type : FieldType.values()) {
            if (type != FieldType.STRING) {
                assertFalse(type.needsLength(), type.name());
            }
        }
    }
}
