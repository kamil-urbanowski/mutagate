package io.github.describeadmin.codegen.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class FieldTypeMutaGate1Test {

    @Test
    void allKeysReturnsKeysOfAllTypesInDeclarationOrder() {
        List<String> expected = List.of(
                "string", "text", "int", "long", "decimal", "flag", "date", "datetime");
        assertEquals(expected, FieldType.allKeys());
    }
}
