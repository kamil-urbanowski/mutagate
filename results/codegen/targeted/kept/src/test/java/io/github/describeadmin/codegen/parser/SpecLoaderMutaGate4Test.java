package io.github.describeadmin.codegen.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class SpecLoaderMutaGate4Test {

    private static Map<String, Object> spec(String entity) {
        return Map.of(
                "basePackage", "com.example.demo",
                "module", "project",
                "entity", entity,
                "table", "biz_project",
                "layout", "nested",
                "fields", List.of(Map.of("name", "projectName", "type", "string")));
    }

    @Test
    void entitySuffixIsRejectedAndPlainEntityIsAccepted() {
        // A plain entity name must parse without error.
        assertDoesNotThrow(() -> SpecLoader.parse(spec("Project"), "t.yaml", null));

        // An entity name ending in "Entity" is valid UpperCamel, so only the suffix check can reject it.
        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(spec("ProjectEntity"), "t.yaml", null));
        List<String> errors = ex.errors();
        assertTrue(errors.stream().anyMatch(e -> e.contains("不要带 Entity 后缀")),
                "expected suffix error but got: " + errors);
        assertEquals(1, errors.stream().filter(e -> e.contains("entity \"ProjectEntity\"")).count());
    }
}
