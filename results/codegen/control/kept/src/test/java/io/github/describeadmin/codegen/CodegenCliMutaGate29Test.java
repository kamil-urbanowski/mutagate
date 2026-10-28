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

class CodegenCliMutaGate29Test {

    @Test
    void planProducesEightFilesInOrderWithExpectedPathsAndContents() {
        ModuleSpec s = new ModuleSpec(
                "com.example", "user", "User", "sys_user", "用户",
                "/system/user", Layout.NESTED, "default", List.of());

        Path outDir = Path.of("backend-out");
        Path frontendOut = Path.of("separate-frontend");

        Map<Path, String> files = CodegenCli.plan(s, outDir, frontendOut);

        Path javaRoot = outDir.resolve("src/main/java");
        Path db = outDir.resolve("src/main/resources/db");

        List<Path> expectedKeys = List.of(
                javaRoot.resolve(s.packageOf("entity").replace('.', '/')).resolve(s.entityClass() + ".java"),
                javaRoot.resolve(s.packageOf("mapper").replace('.', '/')).resolve(s.mapperClass() + ".java"),
                javaRoot.resolve(s.packageOf("service").replace('.', '/')).resolve(s.serviceClass() + ".java"),
                javaRoot.resolve(s.packageOf("controller").replace('.', '/')).resolve(s.controllerClass() + ".java"),
                db.resolve("schema-sys_user.sql"),
                db.resolve("menu-sys_user.sql"),
                frontendOut.resolve("src/api").resolve("user.ts"),
                frontendOut.resolve("src/views").resolve("user").resolve("index.vue"));

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
