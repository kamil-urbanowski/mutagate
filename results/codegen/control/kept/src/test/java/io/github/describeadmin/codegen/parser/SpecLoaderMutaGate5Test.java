package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.Layout;
import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate5Test {

    @TempDir
    Path dir;

    @Test
    void loadReadsUtf8FileAndReportsFileNameInErrors() throws Exception {
        Path good = dir.resolve("good-spec.yaml");
        String yaml = """
                basePackage: com.example.demo
                module: work_order
                entity: WorkOrder
                table: biz_work_order
                comment: 工单管理
                layout: nested
                fields:
                  - name: title
                    type: string
                    length: 64
                    comment: 工单标题
                """;
        Files.writeString(good, yaml, StandardCharsets.UTF_8);

        ModuleSpec spec = SpecLoader.load(good);

        assertEquals("工单管理", spec.comment());
        assertEquals("工单标题", spec.fields().get(0).comment());
        assertEquals("/api/work-order", spec.apiPrefix());
        assertEquals(Layout.NESTED, spec.layout());

        Path bad = dir.resolve("bad-spec.yaml");
        Files.writeString(bad, "module: project\nlayout: nested\n", StandardCharsets.UTF_8);

        SpecException ex = assertThrows(SpecException.class, () -> SpecLoader.load(bad));
        assertFalse(ex.errors().isEmpty());
        for (String e : ex.errors()) {
            assertTrue(e.startsWith("bad-spec.yaml: "), e);
        }
    }
}
