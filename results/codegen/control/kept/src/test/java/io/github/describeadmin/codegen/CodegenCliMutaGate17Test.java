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

class CodegenCliMutaGate17Test {

    @Test
    void planProducesEightFilesInOrderWithSeparateFrontendRootAndGeneratorContents() {
        ModuleSpec s = new ModuleSpec(
                "com.example.app", "order", "Order", "t_order", "订单",
                "/api/order", Layout.NESTED, "default", List.of());

        Path outDir = Path.of("backend-out");
        Path frontendOut = Path.of("separate-frontend-root");

        Map<Path, String> files = CodegenCli.plan(s, outDir, frontendOut);

        Path javaRoot = outDir.resolve("src/main/java");
        List<Path> expectedKeys = new ArrayList<>();
        expectedKeys.add(javaRoot.resolve(s.packageOf("entity").replace('.', '/'))
                .resolve(s.entityClass() + ".java"));
        expectedKeys.add(javaRoot.resolve(s.packageOf("mapper").replace('.', '/'))
                .resolve(s.mapperClass() + ".java"));
        expectedKeys.add(javaRoot.resolve(s.packageOf("service").replace('.', '/'))
                .resolve(s.serviceClass() + ".java"));
        expectedKeys.add(javaRoot.resolve(s.packageOf("controller").replace('.', '/'))
                .resolve(s.controllerClass() + ".java"));
        expectedKeys.add(outDir.resolve("src/main/resources/db/schema-t_order.sql"));
        expectedKeys.add(outDir.resolve("src/main/resources/db/menu-t_order.sql"));
        expectedKeys.add(frontendOut.resolve("src/api/order.ts"));
        expectedKeys.add(frontendOut.resolve("src/views/order/index.vue"));

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
