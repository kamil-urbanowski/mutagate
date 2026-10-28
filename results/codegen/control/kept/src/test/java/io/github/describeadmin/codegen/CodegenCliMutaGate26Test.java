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

class CodegenCliMutaGate26Test {

    @Test
    void planReturnsEightFilesInOrderWithExpectedPathsAndContents() {
        ModuleSpec s = new ModuleSpec(
                "com.example.app",
                "product",
                "Product",
                "t_product",
                "商品",
                "/api/product",
                Layout.NESTED,
                "default",
                new ArrayList<>());

        Path outDir = Path.of("backend-out");
        Path frontendOut = Path.of("separate-frontend");

        Map<Path, String> files = CodegenCli.plan(s, outDir, frontendOut);

        Path javaRoot = outDir.resolve("src/main/java");
        Path db = outDir.resolve("src/main/resources/db");

        List<Path> expectedPaths = List.of(
                javaRoot.resolve(s.packageOf("entity").replace('.', '/')).resolve(s.entityClass() + ".java"),
                javaRoot.resolve(s.packageOf("mapper").replace('.', '/')).resolve(s.mapperClass() + ".java"),
                javaRoot.resolve(s.packageOf("service").replace('.', '/')).resolve(s.serviceClass() + ".java"),
                javaRoot.resolve(s.packageOf("controller").replace('.', '/')).resolve(s.controllerClass() + ".java"),
                db.resolve("schema-t_product.sql"),
                db.resolve("menu-t_product.sql"),
                frontendOut.resolve("src/api").resolve("product.ts"),
                frontendOut.resolve("src/views").resolve("product").resolve("index.vue"));

        List<String> expectedContents = List.of(
                JavaGenerator.entity(s),
                JavaGenerator.mapper(s),
                JavaGenerator.service(s),
                JavaGenerator.controller(s),
                DdlGenerator.generate(s),
                MenuSqlGenerator.generate(s),
                VueGenerator.api(s),
                VueGenerator.page(s));

        assertEquals(expectedPaths, new ArrayList<>(files.keySet()));
        assertEquals(expectedContents, new ArrayList<>(files.values()));
    }
}
