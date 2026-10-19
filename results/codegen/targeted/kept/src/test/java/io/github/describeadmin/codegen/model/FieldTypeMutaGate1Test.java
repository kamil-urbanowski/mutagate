package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldTypeMutaGate1Test {

    @Test
    void allKeysReturnsKeysInDeclarationOrder() {
        List<String> expected = List.of(
                "string", "text", "int", "long", "decimal", "flag", "date", "datetime");
        assertEquals(expected, FieldType.allKeys());
    }
}
