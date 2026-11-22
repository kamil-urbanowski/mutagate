package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate66Test {

  @Test
  void resolveSiblingStringReturnsSiblingPath() throws Exception {
    Path tar = Files.createTempFile("mutagate66", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tar.toAbsolutePath().toUri().getPath() + "!");
      TarFileSystem fs = new TarFileSystem(provider, uri, tar);

      TarPath path = new TarPath(fs, false, "a/b");
      Path result = path.resolveSibling("c");

      assertNotNull(result);
      assertEquals(new TarPath(fs, false, "a/c"), result);
      assertEquals("a/c", result.toString());
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
