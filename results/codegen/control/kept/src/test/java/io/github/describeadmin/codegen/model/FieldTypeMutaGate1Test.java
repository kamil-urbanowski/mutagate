package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FieldTypeMutaGate1Test {

    private static void check(FieldType t, String key, String simple, String importName,
                              String sql, boolean length, boolean precision) {
        assertEquals(key, t.key(), t + " key");
        assertEquals(simple, t.javaSimpleType(), t + " simple type");
        if (importName == null) {
            assertNull(t.importName(), t + " import");
        } else {
            assertEquals(importName, t.importName(), t + " import");
        }
        assertEquals(sql, t.sqlType(), t + " sql type");
        assertEquals(length, t.needsLength(), t + " needsLength");
        assertEquals(precision, t.needsPrecision(), t + " needsPrecision");
    }

    @Test
    void constructorMapsEveryConstantToItsDeclaredAttributes() {
        assertEquals(8, FieldType.values().length);
        check(FieldType.STRING, "string", "String", null, "VARCHAR", true, false);
        check(FieldType.TEXT, "text", "String", null, "TEXT", false, false);
        check(FieldType.INT, "int", "Integer", null, "INT", false, false);
        check(FieldType.LONG, "long", "Long", null, "BIGINT", false, false);
        check(FieldType.DECIMAL, "decimal", "BigDecimal", "java.math.BigDecimal", "DECIMAL", false, true);
        check(FieldType.FLAG, "flag", "Integer", null, "TINYINT", false, false);
        check(FieldType.DATE, "date", "LocalDate", "java.time.LocalDate", "DATE", false, false);
        check(FieldType.DATETIME, "datetime", "LocalDateTime", "java.time.LocalDateTime", "DATETIME", false, false);
    }
}
