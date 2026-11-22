package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate13Test {

  @Test
  void pathFromEnvConvertsNonPathValueToPath() {
    Path result = ArchiveUriSupport.pathFromEnv(Map.of("path", "some/archive.tar"));
    assertEquals(Path.of("some/archive.tar"), result);
  }
}
