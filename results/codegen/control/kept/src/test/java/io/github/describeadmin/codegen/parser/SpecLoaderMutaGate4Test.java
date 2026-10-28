package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.Layout;
import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecLoaderMutaGate4Test {

    @TempDir
    Path tmp;

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
        Path good = tmp.resolve("good.yaml");
        Files.writeString(good, yaml, StandardCharsets.UTF_8);

        ModuleSpec spec = SpecLoader.load(good, "flat");
        assertEquals("工单管理", spec.comment());
        assertEquals("工单标题", spec.fields().get(0).comment());
        assertEquals(Layout.FLAT, spec.layout());
        assertEquals("--layout 参数", spec.layoutOrigin());
        assertEquals("/api/work-order", spec.apiPrefix());
        assertEquals("com.example.demo.controller", spec.packageOf("controller"));

        // 单参数重载等价于 layoutOverride = null：回落到默认（或环境变量）
        ModuleSpec viaSingleArg = SpecLoader.load(good);
        assertEquals("工单管理", viaSingleArg.comment());

        // 错误信息里的来源只用文件名，不带目录
        Path dir = Files.createDirectory(tmp.resolve("some-nested-dir"));
        Path bad = dir.resolve("bad.yaml");
        Files.writeString(bad, "just a string", StandardCharsets.UTF_8);

        SpecException ex = assertThrows(SpecException.class, () -> SpecLoader.load(bad, null));
        List<String> errors = ex.errors();
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).startsWith("bad.yaml: "), errors.get(0));
        assertFalse(errors.get(0).contains("some-nested-dir"), errors.get(0));
    }
}
