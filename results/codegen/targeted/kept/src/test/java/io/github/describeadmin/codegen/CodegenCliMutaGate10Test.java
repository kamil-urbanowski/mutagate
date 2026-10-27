package io.github.describeadmin.codegen;

import io.github.describeadmin.codegen.model.Layout;
import io.github.describeadmin.codegen.model.ModuleSpec;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodegenCliMutaGate10Test {

    @Test
    void planReturnsAllGeneratedFiles() {
        ModuleSpec spec = new ModuleSpec(
                "com.example.app",
                "device",
                "Device",
                "device_info",
                "设备",
                "/device",
                Layout.NESTED,
                "default",
                List.of());

        Path outDir = Path.of("out-dir");
        Path frontendOut = Path.of("front-dir");

        Map<Path, String> files = CodegenCli.plan(spec, outDir, frontendOut);

        assertFalse(files.isEmpty());
        assertEquals(8, files.size());

        Path schema = outDir.resolve("src/main/resources/db/schema-device_info.sql");
        Path menu = outDir.resolve("src/main/resources/db/menu-device_info.sql");
        Path api = frontendOut.resolve("src/api/device.ts");
        Path page = frontendOut.resolve("src/views/device/index.vue");

        assertTrue(files.containsKey(schema));
        assertTrue(files.containsKey(menu));
        assertTrue(files.containsKey(api));
        assertTrue(files.containsKey(page));
        assertNotNull(files.get(schema));
    }
}
