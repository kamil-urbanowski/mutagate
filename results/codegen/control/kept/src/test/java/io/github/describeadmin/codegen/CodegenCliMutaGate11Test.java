package io.github.describeadmin.codegen;

import io.github.describeadmin.codegen.generator.DdlGenerator;
import io.github.describeadmin.codegen.generator.JavaGenerator;
import io.github.describeadmin.codegen.generator.MenuSqlGenerator;
import io.github.describeadmin.codegen.generator.VueGenerator;
import io.github.describeadmin.codegen.model.Layout;
import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodegenCliMutaGate11Test {

    @Test
    void planReturnsAllEightFilesInOrderWithExpectedPathsAndContents() {
        ModuleSpec s = new ModuleSpec(
                "com.example.app", "note", "Note", "t_note", "笔记",
                "/note", Layout.NESTED, "default", List.of());

        Path outDir = Path.of("backendOut");
        Path frontendOut = Path.of("elsewhere", "webapp");

        Map<Path, String> actual = CodegenCli.plan(s, outDir, frontendOut);

        Path javaRoot = outDir.resolve("src/main/java");
        Map<Path, String> expected = new LinkedHashMap<>();
        expected.put(javaRoot.resolve(s.packageOf("entity").replace('.', '/'))
                .resolve(s.entityClass() + ".java"), JavaGenerator.entity(s));
        expected.put(javaRoot.resolve(s.packageOf("mapper").replace('.', '/'))
                .resolve(s.mapperClass() + ".java"), JavaGenerator.mapper(s));
        expected.put(javaRoot.resolve(s.packageOf("service").replace('.', '/'))
                .resolve(s.serviceClass() + ".java"), JavaGenerator.service(s));
        expected.put(javaRoot.resolve(s.packageOf("controller").replace('.', '/'))
                .resolve(s.controllerClass() + ".java"), JavaGenerator.controller(s));
        expected.put(outDir.resolve("src/main/resources/db/schema-t_note.sql"),
                DdlGenerator.generate(s));
        expected.put(outDir.resolve("src/main/resources/db/menu-t_note.sql"),
                MenuSqlGenerator.generate(s));
        expected.put(frontendOut.resolve("src/api/note.ts"), VueGenerator.api(s));
        expected.put(frontendOut.resolve("src/views/note/index.vue"), VueGenerator.page(s));

        assertEquals(8, actual.size());
        assertEquals(new ArrayList<>(expected.keySet()), new ArrayList<>(actual.keySet()));
        assertEquals(expected, actual);
    }
}
