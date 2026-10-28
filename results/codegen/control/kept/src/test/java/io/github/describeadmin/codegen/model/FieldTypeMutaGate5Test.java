package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldTypeMutaGate5Test {

    @Test
    void keyReturnsExpectedLowercaseIdentifierForEveryConstant() {
        Map<FieldType, String> expected = new LinkedHashMap<>();
        expected.put(FieldType.STRING, "string");
        expected.put(FieldType.TEXT, "text");
        expected.put(FieldType.INT, "int");
        expected.put(FieldType.LONG, "long");
        expected.put(FieldType.DECIMAL, "decimal");
        expected.put(FieldType.FLAG, "flag");
        expected.put(FieldType.DATE, "date");
        expected.put(FieldType.DATETIME, "datetime");

        assertEquals(FieldType.values().length, expected.size());

        for (Map.Entry<FieldType, String> e : expected.entrySet()) {
            String key = e.getKey().key();
            assertEquals(e.getValue(), key);
            assertEquals(key.toLowerCase(), key);
            assertTrue(FieldType.of(key).isPresent());
            assertEquals(e.getKey(), FieldType.of(key).get());
        }
    }
}
