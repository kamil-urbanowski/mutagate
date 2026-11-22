package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate5Test {

  @Test
  void endsWithStringMatchesTrailingSegments() throws Exception {
    Path tar = Files.createTempFile("mutagate5", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toUri()), tar);
      TarPath p = new TarPath(fs, true, "a/b/c.txt");

      assertTrue(p.endsWith("c.txt"));
      assertTrue(p.endsWith("b/c.txt"));
      assertFalse(p.endsWith("b"));
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
