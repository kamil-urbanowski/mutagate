package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate8Test {

  @Test
  void extractArchivePathReturnsPathFromEnvInsteadOfUri() {
    Map<String, Object> env = new HashMap<>();
    Path expected = Path.of("env", "archive.tar");
    env.put("path", expected);

    Path result =
        ArchiveUriSupport.extractArchivePath(URI.create("tarball:/uri/other.tar!/entry"), env, "tarball");

    assertEquals(expected, result);
  }
}
