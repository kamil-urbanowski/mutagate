package io.github.describeadmin.codegen.parser;

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

class SpecLoaderMutaGate11Test {

    @TempDir
    Path tempDir;

    @Test
    void loadReadsUtf8FileAndReportsErrorsWithFileNameOnly() throws Exception {
        String yaml = """
                basePackage: com.example.demo
                module: work_order
                entity: WorkOrder
                table: biz_work_order
                comment: 工单管理
                fields:
                  - name: title
                    type: string
                    length: 64
                    comment: 工单标题
                """;
        Path good = tempDir.resolve("good.yaml");
        Files.writeString(good, yaml, StandardCharsets.UTF_8);

        ModuleSpec spec = SpecLoader.load(good);

        assertEquals("工单管理", spec.comment());
        assertEquals("工单标题", spec.fields().get(0).comment());
        assertEquals("/api/work-order", spec.apiPrefix());
        assertEquals("WorkOrder", spec.entity());
        assertEquals("title", spec.fields().get(0).name());

        Path bad = tempDir.resolve("bad.yaml");
        Files.writeString(bad, "- just\n- a list\n", StandardCharsets.UTF_8);

        SpecException ex = assertThrows(SpecException.class, () -> SpecLoader.load(bad));
        assertEquals(1, ex.errors().size());
        String err = ex.errors().get(0);
        assertTrue(err.startsWith("bad.yaml: "), err);
        assertTrue(err.contains("根节点必须是对象"), err);
        assertFalse(err.contains(tempDir.toString()), err);
    }
}
