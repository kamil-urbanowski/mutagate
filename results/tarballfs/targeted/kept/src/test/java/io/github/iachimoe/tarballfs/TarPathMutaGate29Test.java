package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate29Test {

  @Test
  void getParentDropsOnlyLastSegment() throws Exception {
    Path tar = Files.createTempFile("tarpath-test", ".tar");
    try {
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath()), tar);

      TarPath absolute = new TarPath(fs, true, "a/b/c");
      Path parent = absolute.getParent();
      assertEquals("/a/b", parent.toString());
      assertEquals(2, parent.getNameCount());
      assertEquals(new TarPath(fs, true, "a/b"), parent);

      TarPath relative = new TarPath(fs, false, "x/y");
      Path relParent = relative.getParent();
      assertEquals("x", relParent.toString());
      assertEquals(1, relParent.getNameCount());
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
