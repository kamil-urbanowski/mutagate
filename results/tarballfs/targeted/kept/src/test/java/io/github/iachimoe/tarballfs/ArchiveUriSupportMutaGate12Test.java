package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate12Test {

  @Test
  void pathFromEnvReturnsSamePathInstanceWhenEnvValueIsPath() {
    Path expected = Path.of("some", "archive.tar");
    Map<String, Object> env = Map.of("path", expected);

    Path actual = ArchiveUriSupport.pathFromEnv(env);

    assertSame(expected, actual);
  }
}
