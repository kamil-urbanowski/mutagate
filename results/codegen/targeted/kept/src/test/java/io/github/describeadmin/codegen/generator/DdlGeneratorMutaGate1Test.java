package io.github.describeadmin.codegen.generator;

import io.github.describeadmin.codegen.model.Layout;
import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DdlGeneratorMutaGate1Test {

    @Test
    void escapesSingleQuotesInTableComment() {
        ModuleSpec spec = new ModuleSpec(
                "com.example.demo",
                "demo",
                "Demo",
                "biz_demo",
                "it's a demo",
                "/api/demo",
                Layout.values()[0],
                "default",
                List.of());

        String ddl = DdlGenerator.generate(spec);

        assertTrue(ddl.contains("COMMENT='it''s a demo';"), ddl);
    }
}
