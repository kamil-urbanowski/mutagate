package io.github.describeadmin.codegen.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;

class FieldTypeMutaGate2Test {

    @Test
    void javaSimpleTypeStripsLeadingDotAtIndexZero() throws Exception {
        Field f = FieldType.class.getDeclaredField("javaType");
        f.setAccessible(true);
        FieldType type = FieldType.DATE;
        Object original = f.get(type);
        try {
            f.set(type, ".Foo");
            assertEquals("Foo", type.javaSimpleType());
        } finally {
            f.set(type, original);
        }
        assertEquals("LocalDate", type.javaSimpleType());
    }
}
