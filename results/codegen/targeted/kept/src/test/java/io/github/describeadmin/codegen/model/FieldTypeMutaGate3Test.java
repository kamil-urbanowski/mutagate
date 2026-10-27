package io.github.describeadmin.codegen.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class FieldTypeMutaGate3Test {

    @Test
    void keyReturnsYamlIdentifierForEveryType() {
        assertEquals("string", FieldType.STRING.key());
        assertEquals("text", FieldType.TEXT.key());
        assertEquals("int", FieldType.INT.key());
        assertEquals("long", FieldType.LONG.key());
        assertEquals("decimal", FieldType.DECIMAL.key());
        assertEquals("flag", FieldType.FLAG.key());
        assertEquals("date", FieldType.DATE.key());
        assertEquals("datetime", FieldType.DATETIME.key());
        assertEquals(
                List.of("string", "text", "int", "long", "decimal", "flag", "date", "datetime"),
                FieldType.allKeys());
    }
}
