package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldTypeMutaGate2Test {

    @Test
    void javaSimpleTypeHandlesDotAtIndexZero() throws Exception {
        // Normal cases: no dot (index -1) and a fully qualified name
        assertEquals("String", FieldType.TEXT.javaSimpleType());
        assertEquals("BigDecimal", FieldType.DECIMAL.javaSimpleType());

        // Boundary case: a dot at index 0 must still be stripped.
        // The enum constants never hit this, so set the field via reflection temporarily.
        Field f = FieldType.class.getDeclaredField("javaType");
        f.setAccessible(true);
        String original = (String) f.get(FieldType.STRING);
        try {
            f.set(FieldType.STRING, ".Foo");
            assertEquals("Foo", FieldType.STRING.javaSimpleType());
        } finally {
            f.set(FieldType.STRING, original);
        }
        assertEquals("String", FieldType.STRING.javaSimpleType());
    }
}
