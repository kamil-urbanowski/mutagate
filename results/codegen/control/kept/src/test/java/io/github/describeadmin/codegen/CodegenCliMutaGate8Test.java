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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodegenCliMutaGate8Test {

    @Test
    void planMapsEachGeneratorOutputToItsPathInOrder() {
        ModuleSpec s = new ModuleSpec("com.example", "book", "Book", "t_book", "图书",
                "/api/book", Layout.NESTED, "default", List.of());
        Path outDir = Path.of("backend-root");
        Path frontendOut = Path.of("separate-frontend-root");

        Map<Path, String> plan = CodegenCli.plan(s, outDir, frontendOut);

        Path javaRoot = outDir.resolve("src/main/java");
        Path db = outDir.resolve("src/main/resources/db");

        List<Path> expectedKeys = new ArrayList<>();
        expectedKeys.add(javaRoot.resolve(s.packageOf("entity").replace('.', '/'))
                .resolve(s.entityClass() + ".java"));
        expectedKeys.add(javaRoot.resolve(s.packageOf("mapper").replace('.', '/'))
                .resolve(s.mapperClass() + ".java"));
        expectedKeys.add(javaRoot.resolve(s.packageOf("service").replace('.', '/'))
                .resolve(s.serviceClass() + ".java"));
        expectedKeys.add(javaRoot.resolve(s.packageOf("controller").replace('.', '/'))
                .resolve(s.controllerClass() + ".java"));
        expectedKeys.add(db.resolve("schema-t_book.sql"));
        expectedKeys.add(db.resolve("menu-t_book.sql"));
        expectedKeys.add(frontendOut.resolve("src/api").resolve("book.ts"));
        expectedKeys.add(frontendOut.resolve("src/views").resolve("book").resolve("index.vue"));

        assertEquals(expectedKeys, new ArrayList<>(plan.keySet()));

        List<String> expectedValues = List.of(
                JavaGenerator.entity(s),
                JavaGenerator.mapper(s),
                JavaGenerator.service(s),
                JavaGenerator.controller(s),
                DdlGenerator.generate(s),
                MenuSqlGenerator.generate(s),
                VueGenerator.api(s),
                VueGenerator.page(s));
        assertEquals(expectedValues, new ArrayList<>(plan.values()));
    }
}
