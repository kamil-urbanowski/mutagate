package io.github.describeadmin.codegen.parser;

import io.github.describeadmin.codegen.model.Layout;
import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpecLoaderMutaGate10Test {

    @Test
    void loadReadsUtf8ReportsFileNameAndPropagatesIoErrors(@TempDir Path dir) throws IOException {
        // 1. UTF-8 中文内容被正确读取，并且 layoutOverride 生效
        Path good = dir.resolve("good.yaml");
        String yaml = "basePackage: com.example.demo\n"
                + "module: work_order\n"
                + "entity: WorkOrder\n"
                + "table: biz_work_order\n"
                + "comment: 工单管理\n"
                + "fields:\n"
                + "  - name: title\n"
                + "    type: string\n"
                + "    length: 64\n"
                + "    comment: 工单标题\n";
        Files.writeString(good, yaml, StandardCharsets.UTF_8);

        ModuleSpec spec = SpecLoader.load(good, "flat");
        assertEquals("工单管理", spec.comment());
        assertEquals("工单标题", spec.fields().get(0).comment());
        assertEquals(Layout.FLAT, spec.layout());
        assertEquals("--layout 参数", spec.layoutOrigin());
        assertEquals("/api/work-order", spec.apiPrefix());

        // 2. 校验失败时，错误前缀是文件名（不含目录）
        Path subDir = Files.createDirectory(dir.resolve("sub"));
        Path bad = subDir.resolve("bad.yaml");
        Files.writeString(bad, "- a\n- b\n", StandardCharsets.UTF_8);

        SpecException ex = assertThrows(SpecException.class, () -> SpecLoader.load(bad, null));
        assertEquals(List.of("bad.yaml: 根节点必须是对象（key: value 形式）"), ex.errors());

        // 3. 文件不存在时抛出 IOException，而不是被吞掉
        Path missing = dir.resolve("missing.yaml");
        assertThrows(IOException.class, () -> SpecLoader.load(missing, null));
    }
}
