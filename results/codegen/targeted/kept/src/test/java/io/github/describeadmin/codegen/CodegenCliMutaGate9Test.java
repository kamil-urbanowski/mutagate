package io.github.describeadmin.codegen;

import io.github.describeadmin.codegen.model.Layout;
import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate9Test {

    @Test
    void planPlacesJavaFilesUnderPackageDirectories() {
        ModuleSpec spec = new ModuleSpec(
                "com.example.demo",
                "product",
                "Product",
                "product",
                "Product",
                "/api/product",
                Layout.NESTED,
                "test",
                List.of());

        Path outDir = Path.of("out");
        Path frontendOut = Path.of("out", "frontend");

        Map<Path, String> files = CodegenCli.plan(spec, outDir, frontendOut);

        String entityPkg = spec.packageOf("entity");
        assertTrue(entityPkg.contains("."), "package should be dotted: " + entityPkg);

        Path expectedEntity = outDir.resolve("src/main/java")
                .resolve(entityPkg.replace('.', '/'))
                .resolve(spec.entityClass() + ".java");
        assertTrue(files.containsKey(expectedEntity),
                "expected " + expectedEntity + " in " + files.keySet());

        String controllerPkg = spec.packageOf("controller");
        Path expectedController = outDir.resolve("src/main/java")
                .resolve(controllerPkg.replace('.', '/'))
                .resolve(spec.controllerClass() + ".java");
        assertTrue(files.containsKey(expectedController),
                "expected " + expectedController + " in " + files.keySet());
    }
}
