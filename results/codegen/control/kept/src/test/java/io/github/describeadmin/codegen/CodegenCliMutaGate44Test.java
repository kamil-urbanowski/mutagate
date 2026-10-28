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

class CodegenCliMutaGate44Test {

    @Test
    void planMapsEachFileToItsGeneratorInOrderWithSeparateFrontendRoot() {
        ModuleSpec s = new ModuleSpec(
                "com.example.app", "product", "Product", "product_info",
                "商品", "/product", Layout.NESTED, "spec", List.of());
        Path outDir = Path.of("backend-out");
        Path frontendOut = Path.of("elsewhere", "web");

        Map<Path, String> files = CodegenCli.plan(s, outDir, frontendOut);

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
        expectedKeys.add(db.resolve("schema-product_info.sql"));
        expectedKeys.add(db.resolve("menu-product_info.sql"));
        expectedKeys.add(frontendOut.resolve("src/api").resolve("product.ts"));
        expectedKeys.add(frontendOut.resolve("src/views").resolve("product").resolve("index.vue"));

        assertEquals(expectedKeys, new ArrayList<>(files.keySet()));

        List<String> expectedValues = List.of(
                JavaGenerator.entity(s),
                JavaGenerator.mapper(s),
                JavaGenerator.service(s),
                JavaGenerator.controller(s),
                DdlGenerator.generate(s),
                MenuSqlGenerator.generate(s),
                VueGenerator.api(s),
                VueGenerator.page(s));
        assertEquals(expectedValues, new ArrayList<>(files.values()));
    }
}
