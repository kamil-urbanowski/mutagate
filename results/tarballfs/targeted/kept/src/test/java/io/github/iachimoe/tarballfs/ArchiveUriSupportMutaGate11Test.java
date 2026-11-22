package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate11Test {

  @Test
  void pathFromEnvHandlesPathAndNonPathValues() {
    Path original = Path.of("some", "dir", "archive.tar");
    Map<String, Object> pathEnv = Map.of("path", original);
    assertSame(original, ArchiveUriSupport.pathFromEnv(pathEnv));

    Map<String, Object> stringEnv = Map.of("path", "some/dir/archive.tar");
    assertEquals(original, ArchiveUriSupport.pathFromEnv(stringEnv));

    Map<String, Object> fileEnv = Map.of("file", "some/dir/archive.tar");
    assertEquals(original, ArchiveUriSupport.pathFromEnv(fileEnv));
  }
}
