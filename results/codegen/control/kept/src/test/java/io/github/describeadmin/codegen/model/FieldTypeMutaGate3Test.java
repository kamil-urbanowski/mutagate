package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldTypeMutaGate3Test {

    @Test
    void ofLooksUpKeyCaseInsensitivelyAndRejectsUnknownOrNull() {
        // every type is found by its own key, and the exact same constant is returned
        for (FieldType t : FieldType.values()) {
            assertSame(t, FieldType.of(t.key()).orElseThrow());
            assertSame(t, FieldType.of(t.key().toUpperCase()).orElseThrow());
        }

        // mixed case
        assertEquals(Optional.of(FieldType.DATETIME), FieldType.of("DateTime"));
        assertEquals(Optional.of(FieldType.FLAG), FieldType.of("fLaG"));

        // enum constant name differs from key for none, but unknown keys must be empty
        assertTrue(FieldType.of("timestamp").isEmpty());
        assertTrue(FieldType.of("boolean").isEmpty());
        assertTrue(FieldType.of("json").isEmpty());
        assertTrue(FieldType.of("").isEmpty());

        // no trimming and no partial matching
        assertFalse(FieldType.of(" string").isPresent());
        assertFalse(FieldType.of("string ").isPresent());
        assertFalse(FieldType.of("str").isPresent());
        assertFalse(FieldType.of("dateX").isPresent());

        // null key is tolerated (equalsIgnoreCase(null) is false)
        assertTrue(FieldType.of(null).isEmpty());
    }
}
