package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldTypeMutaGate4Test {

    @Test
    void allKeysListsEveryKeyInDeclarationOrderAndIsImmutable() {
        List<String> keys = FieldType.allKeys();

        assertEquals(
                List.of("string", "text", "int", "long", "decimal", "flag", "date", "datetime"),
                keys);
        assertEquals(FieldType.values().length, keys.size());
        assertEquals(keys.size(), new HashSet<>(keys).size());

        for (FieldType t : FieldType.values()) {
            assertEquals(t.key(), keys.get(t.ordinal()));
            assertTrue(FieldType.of(keys.get(t.ordinal())).isPresent());
        }

        for (String forbidden : Arrays.asList("timestamp", "json", "boolean")) {
            assertFalse(keys.contains(forbidden));
        }

        assertThrows(UnsupportedOperationException.class, () -> keys.add("extra"));
        assertThrows(UnsupportedOperationException.class, () -> keys.remove(0));
    }
}
