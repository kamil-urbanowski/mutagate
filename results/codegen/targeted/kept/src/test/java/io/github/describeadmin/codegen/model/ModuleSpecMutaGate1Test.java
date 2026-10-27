package io.github.describeadmin.codegen.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ModuleSpecMutaGate1Test {

    private static FieldSpec field(String name, FieldSpec.QueryMode mode) {
        return new FieldSpec(name, name, null, null, null, null, true, name, mode, false);
    }

    @Test
    void queryFieldsExcludesFieldsWithoutQueryMode() {
        FieldSpec none = field("a", FieldSpec.QueryMode.NONE);
        FieldSpec eq = field("b", FieldSpec.QueryMode.EQ);
        FieldSpec like = field("c", FieldSpec.QueryMode.LIKE);
        FieldSpec range = field("d", FieldSpec.QueryMode.RANGE);

        ModuleSpec spec = new ModuleSpec("com.example", "demo", "Demo", "biz_demo", "演示",
                "/api/demo", Layout.NESTED, "默认", List.of(none, eq, like, range));

        List<FieldSpec> result = spec.queryFields();

        assertEquals(3, result.size());
        assertSame(eq, result.get(0));
        assertSame(like, result.get(1));
        assertSame(range, result.get(2));
    }
}
