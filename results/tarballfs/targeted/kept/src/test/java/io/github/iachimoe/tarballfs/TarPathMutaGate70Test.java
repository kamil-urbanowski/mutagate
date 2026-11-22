package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate70Test {

  @Test
  void startsWithStringMatchesPrefixSegments() throws Exception {
    Path tar = Files.createTempFile("tarpath-test", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      URI uri = URI.create("tar:" + tar.toAbsolutePath().toUri().getPath() + "!");
      TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), uri, tar);

      TarPath path = new TarPath(fs, false, "a/b/c");

      assertTrue(path.startsWith("a/b"));
      assertTrue(path.startsWith("a"));
      assertTrue(path.startsWith("a/b/c"));
      assertFalse(path.startsWith("b"));
      assertFalse(path.startsWith("a/b/c/d"));
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
