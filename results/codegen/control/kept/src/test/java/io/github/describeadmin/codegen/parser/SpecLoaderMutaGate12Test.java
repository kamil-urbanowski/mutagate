package io.github.describeadmin.codegen.parser;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate12Test {

    @Test
    void parseRejectsNonObjectRootAndPrefixesErrorsWithSource() {
        List<String> expected = List.of("my.yaml: 根节点必须是对象（key: value 形式）");

        SpecException str = assertThrows(SpecException.class,
                () -> SpecLoader.parse("just a string", "my.yaml"));
        assertEquals(expected, str.errors());

        SpecException nul = assertThrows(SpecException.class,
                () -> SpecLoader.parse(null, "my.yaml"));
        assertEquals(expected, nul.errors());

        SpecException list = assertThrows(SpecException.class,
                () -> SpecLoader.parse(List.of("a", "b"), "my.yaml"));
        assertEquals(expected, list.errors());

        SpecException empty = assertThrows(SpecException.class,
                () -> SpecLoader.parse(Map.of(), "empty.yaml"));
        List<String> errs = empty.errors();
        assertTrue(errs.size() >= 5);
        for (String e : errs) {
            assertTrue(e.startsWith("empty.yaml: "), e);
        }
        for (String key : List.of("basePackage", "module", "entity", "table")) {
            assertTrue(errs.stream().anyMatch(e -> e.contains("缺少必填项 " + key)), key);
        }
        assertTrue(errs.stream().anyMatch(e -> e.contains("缺少 fields")));
    }
}
