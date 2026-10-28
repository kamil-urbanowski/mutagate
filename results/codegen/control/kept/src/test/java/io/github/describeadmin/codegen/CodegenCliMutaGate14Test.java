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

class CodegenCliMutaGate14Test {

    @Test
    void planProducesEightFilesInOrderWithExpectedPathsAndContents() {
        ModuleSpec s = new ModuleSpec(
                "com.example", "user", "User", "sys_user", "用户",
                "/user", Layout.NESTED, "default", List.of());

        Path outDir = Path.of("backend-out");
        Path frontendOut = Path.of("separate-frontend-root");

        Map<Path, String> files = CodegenCli.plan(s, outDir, frontendOut);

        Path javaRoot = outDir.resolve("src/main/java");
        Path db = outDir.resolve("src/main/resources/db");

        List<Path> expected = new ArrayList<>();
        expected.add(javaRoot.resolve(s.packageOf("entity").replace('.', '/'))
                .resolve(s.entityClass() + ".java"));
        expected.add(javaRoot.resolve(s.packageOf("mapper").replace('.', '/'))
                .resolve(s.mapperClass() + ".java"));
        expected.add(javaRoot.resolve(s.packageOf("service").replace('.', '/'))
                .resolve(s.serviceClass() + ".java"));
        expected.add(javaRoot.resolve(s.packageOf("controller").replace('.', '/'))
                .resolve(s.controllerClass() + ".java"));
        expected.add(db.resolve("schema-sys_user.sql"));
        expected.add(db.resolve("menu-sys_user.sql"));
        expected.add(frontendOut.resolve("src/api").resolve("user.ts"));
        expected.add(frontendOut.resolve("src/views").resolve("user").resolve("index.vue"));

        assertEquals(expected, new ArrayList<>(files.keySet()));

        assertEquals(JavaGenerator.entity(s), files.get(expected.get(0)));
        assertEquals(JavaGenerator.mapper(s), files.get(expected.get(1)));
        assertEquals(JavaGenerator.service(s), files.get(expected.get(2)));
        assertEquals(JavaGenerator.controller(s), files.get(expected.get(3)));
        assertEquals(DdlGenerator.generate(s), files.get(expected.get(4)));
        assertEquals(MenuSqlGenerator.generate(s), files.get(expected.get(5)));
        assertEquals(VueGenerator.api(s), files.get(expected.get(6)));
        assertEquals(VueGenerator.page(s), files.get(expected.get(7)));
    }
}
