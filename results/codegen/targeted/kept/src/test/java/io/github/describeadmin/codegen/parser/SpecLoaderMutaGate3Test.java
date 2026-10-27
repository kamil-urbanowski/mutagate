package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate3Test {

    private static Map<String, Object> spec(String entity) {
        Map<String, Object> field = new LinkedHashMap<>();
        field.put("name", "projectName");
        field.put("type", "string");
        field.put("length", 128);

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("basePackage", "com.example.demo");
        root.put("module", "project");
        root.put("entity", entity);
        root.put("table", "biz_project");
        root.put("fields", List.of(field));
        return root;
    }

    @Test
    void entityMustBeUpperCamel() {
        // A valid UpperCamel entity must be accepted.
        ModuleSpec ok = SpecLoader.parse(spec("Project"), "t.yaml", "nested");
        assertEquals("Project", ok.entity());

        // A lower-case-initial entity must be rejected with exactly the UpperCamel error.
        SpecException ex = assertThrows(SpecException.class,
                () -> SpecLoader.parse(spec("project"), "t.yaml", "nested"));
        assertEquals(1, ex.errors().size());
        String msg = ex.errors().get(0);
        assertTrue(msg.contains("entity \"project\""), msg);
        assertTrue(msg.contains("大驼峰"), msg);
    }
}
