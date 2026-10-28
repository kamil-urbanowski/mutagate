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

class CodegenCliMutaGate38Test {

    @Test
    void planMapsEachArtifactToItsPathAndContentInOrder() {
        ModuleSpec s = new ModuleSpec("com.example.app", "product", "Product", "t_product",
                "商品", "/api/products", Layout.NESTED, "spec", List.of());
        Path out = Path.of("backend-out");
        Path front = Path.of("elsewhere", "web-app");

        Map<Path, String> plan = CodegenCli.plan(s, out, front);

        Path javaRoot = out.resolve("src/main/java");
        List<Path> expectedKeys = new ArrayList<>();
        expectedKeys.add(javaRoot.resolve(s.packageOf("entity").replace('.', '/'))
                .resolve(s.entityClass() + ".java"));
        expectedKeys.add(javaRoot.resolve(s.packageOf("mapper").replace('.', '/'))
                .resolve(s.mapperClass() + ".java"));
        expectedKeys.add(javaRoot.resolve(s.packageOf("service").replace('.', '/'))
                .resolve(s.serviceClass() + ".java"));
        expectedKeys.add(javaRoot.resolve(s.packageOf("controller").replace('.', '/'))
                .resolve(s.controllerClass() + ".java"));
        expectedKeys.add(out.resolve("src/main/resources/db/schema-t_product.sql"));
        expectedKeys.add(out.resolve("src/main/resources/db/menu-t_product.sql"));
        expectedKeys.add(front.resolve("src/api/product.ts"));
        expectedKeys.add(front.resolve("src/views/product/index.vue"));

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
