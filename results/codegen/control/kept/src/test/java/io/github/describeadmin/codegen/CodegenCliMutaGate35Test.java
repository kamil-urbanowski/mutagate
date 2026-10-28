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

class CodegenCliMutaGate35Test {

    @Test
    void planMapsEachArtifactToExpectedPathAndContentInOrder() {
        ModuleSpec s = new ModuleSpec(
                "com.example.demo",
                "product",
                "Product",
                "demo_product",
                "商品",
                "/demo/product",
                Layout.NESTED,
                "spec",
                List.of());

        Path outDir = Path.of("backend-out");
        Path frontendOut = Path.of("separate-frontend");

        Map<Path, String> files = CodegenCli.plan(s, outDir, frontendOut);

        Path javaRoot = outDir.resolve("src/main/java");
        Path db = outDir.resolve("src/main/resources/db");

        List<Path> expectedKeys = new ArrayList<>();
        List<String> expectedValues = new ArrayList<>();

        expectedKeys.add(javaRoot.resolve(s.packageOf("entity").replace('.', '/'))
                .resolve(s.entityClass() + ".java"));
        expectedValues.add(JavaGenerator.entity(s));

        expectedKeys.add(javaRoot.resolve(s.packageOf("mapper").replace('.', '/'))
                .resolve(s.mapperClass() + ".java"));
        expectedValues.add(JavaGenerator.mapper(s));

        expectedKeys.add(javaRoot.resolve(s.packageOf("service").replace('.', '/'))
                .resolve(s.serviceClass() + ".java"));
        expectedValues.add(JavaGenerator.service(s));

        expectedKeys.add(javaRoot.resolve(s.packageOf("controller").replace('.', '/'))
                .resolve(s.controllerClass() + ".java"));
        expectedValues.add(JavaGenerator.controller(s));

        expectedKeys.add(db.resolve("schema-demo_product.sql"));
        expectedValues.add(DdlGenerator.generate(s));

        expectedKeys.add(db.resolve("menu-demo_product.sql"));
        expectedValues.add(MenuSqlGenerator.generate(s));

        expectedKeys.add(frontendOut.resolve("src/api").resolve("product.ts"));
        expectedValues.add(VueGenerator.api(s));

        expectedKeys.add(frontendOut.resolve("src/views").resolve("product").resolve("index.vue"));
        expectedValues.add(VueGenerator.page(s));

        assertEquals(expectedKeys, new ArrayList<>(files.keySet()));
        assertEquals(expectedValues, new ArrayList<>(files.values()));
    }
}
