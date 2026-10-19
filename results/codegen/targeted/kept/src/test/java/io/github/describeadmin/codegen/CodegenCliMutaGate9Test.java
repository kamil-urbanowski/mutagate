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
    void planPlacesJavaFilesInPackageDirectories() {
        ModuleSpec s = new ModuleSpec(
                "com.example.app", "user", "User", "t_user", "用户",
                "/user", Layout.NESTED, "default", List.of());

        Path outDir = Path.of("build-out");
        Path frontendOut = Path.of("fe-out");

        Map<Path, String> files = CodegenCli.plan(s, outDir, frontendOut);

        Path javaRoot = outDir.resolve("src/main/java");
        String pkgDir = s.packageOf("entity").replace('.', '/');
        assertTrue(pkgDir.contains("/"), "package dir should be nested: " + pkgDir);

        Path expectedEntity = javaRoot.resolve(pkgDir).resolve(s.entityClass() + ".java");
        assertTrue(files.containsKey(expectedEntity),
                "expected " + expectedEntity + " in " + files.keySet());

        String ctrlDir = s.packageOf("controller").replace('.', '/');
        Path expectedController = javaRoot.resolve(ctrlDir).resolve(s.controllerClass() + ".java");
        assertTrue(files.containsKey(expectedController),
                "expected " + expectedController + " in " + files.keySet());
    }
}
