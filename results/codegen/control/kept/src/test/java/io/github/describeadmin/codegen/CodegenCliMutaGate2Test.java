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

class CodegenCliMutaGate2Test {

    @Test
    void planProducesEightFilesInOrderWithExpectedPathsAndContents() {
        ModuleSpec s = new ModuleSpec("com.example.app", "product", "Product", "product_info",
                "商品", "/api/product", Layout.NESTED, "spec", List.of());
        Path out = Path.of("outRoot");
        Path fe = Path.of("elsewhere", "webapp");

        Map<Path, String> files = CodegenCli.plan(s, out, fe);

        Path java = out.resolve("src/main/java");
        Path db = out.resolve("src/main/resources/db");
        List<Path> expectedKeys = new ArrayList<>();
        expectedKeys.add(java.resolve(s.packageOf("entity").replace('.', '/')).resolve(s.entityClass() + ".java"));
        expectedKeys.add(java.resolve(s.packageOf("mapper").replace('.', '/')).resolve(s.mapperClass() + ".java"));
        expectedKeys.add(java.resolve(s.packageOf("service").replace('.', '/')).resolve(s.serviceClass() + ".java"));
        expectedKeys.add(java.resolve(s.packageOf("controller").replace('.', '/')).resolve(s.controllerClass() + ".java"));
        expectedKeys.add(db.resolve("schema-product_info.sql"));
        expectedKeys.add(db.resolve("menu-product_info.sql"));
        expectedKeys.add(fe.resolve("src/api").resolve("product.ts"));
        expectedKeys.add(fe.resolve("src/views").resolve("product").resolve("index.vue"));

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
