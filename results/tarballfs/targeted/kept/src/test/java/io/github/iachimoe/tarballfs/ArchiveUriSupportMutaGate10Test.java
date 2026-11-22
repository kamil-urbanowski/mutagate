package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate10Test {

  @Test
  void pathFromEnvUsesPathKeyAndFallsBackToFileKey() {
    Path onlyPath = ArchiveUriSupport.pathFromEnv(Map.of("path", "first.tar"));
    assertEquals(Path.of("first.tar"), onlyPath);

    Path onlyFile = ArchiveUriSupport.pathFromEnv(Map.of("file", "second.tar"));
    assertEquals(Path.of("second.tar"), onlyFile);

    Path both = ArchiveUriSupport.pathFromEnv(Map.of("path", "first.tar", "file", "second.tar"));
    assertEquals(Path.of("first.tar"), both);
  }
}
