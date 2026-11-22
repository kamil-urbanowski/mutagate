package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate9Test {

  @Test
  void pathFromEnvHandlesNullAndNonNullEnv() {
    assertNull(ArchiveUriSupport.pathFromEnv(null));

    Path expected = Path.of("some", "archive.tar");
    assertEquals(expected, ArchiveUriSupport.pathFromEnv(Map.of("path", expected)));
    assertEquals(Path.of("other.tar"), ArchiveUriSupport.pathFromEnv(Map.of("file", "other.tar")));
  }
}
