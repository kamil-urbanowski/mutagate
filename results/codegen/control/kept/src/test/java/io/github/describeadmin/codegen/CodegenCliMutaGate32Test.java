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

class CodegenCliMutaGate32Test {

    @Test
    void planProducesEightFilesInOrderWithExpectedPathsAndContents() {
        ModuleSpec s = new ModuleSpec(
                "com.example.app", "product", "Product", "product_info",
                "商品", "/api/product", Layout.NESTED, "spec", List.of());

        Path outDir = Path.of("out-root");
        Path frontendOut = Path.of("elsewhere", "frontend-app");

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

        assertEquals(JavaGenerator.entity(s), files.get(expectedKeys.get(0)));
        assertEquals(JavaGenerator.mapper(s), files.get(expectedKeys.get(1)));
        assertEquals(JavaGenerator.service(s), files.get(expectedKeys.get(2)));
        assertEquals(JavaGenerator.controller(s), files.get(expectedKeys.get(3)));
        assertEquals(DdlGenerator.generate(s), files.get(expectedKeys.get(4)));
        assertEquals(MenuSqlGenerator.generate(s), files.get(expectedKeys.get(5)));
        assertEquals(VueGenerator.api(s), files.get(expectedKeys.get(6)));
        assertEquals(VueGenerator.page(s), files.get(expectedKeys.get(7)));
    }
}
