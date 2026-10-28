package io.github.describeadmin.codegen.parser;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate6Test {

    @Test
    void parseRejectsNonObjectRootAndPrefixesErrorsWithSource() {
        String expectedRootError = "my.yaml: 根节点必须是对象（key: value 形式）";

        for (Object badRoot : new Object[] {null, "just a string", List.of("a", "b"), 42}) {
            SpecException ex = assertThrows(SpecException.class,
                    () -> SpecLoader.parse(badRoot, "my.yaml"));
            assertEquals(List.of(expectedRootError), ex.errors());
        }

        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(Map.of(), "other.yaml"));
        List<String> errors = ex.errors();
        assertTrue(errors.size() >= 5, "应一次报出所有缺失项: " + errors);
        for (String e : errors) {
            assertTrue(e.startsWith("other.yaml: "), "错误应带来源前缀: " + e);
        }
        assertTrue(errors.contains(
                "other.yaml: 缺少必填项 basePackage（业务方基础包名，如 com.example.demo）"));
        assertTrue(errors.contains("other.yaml: 缺少 fields"));
    }
}
