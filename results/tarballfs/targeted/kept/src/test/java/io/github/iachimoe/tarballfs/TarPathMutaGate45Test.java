package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate45Test {

  @Test
  void normalizeHandlesDotDotSegments() throws Exception {
    Path tar = Files.createTempFile("mutagate45", ".tar");
    try {
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

      // ".." cancels a preceding normal segment
      assertEquals("a/c", new TarPath(fs, false, "a/b/../c").normalize().toString());
      assertEquals("/a/c", new TarPath(fs, true, "a/b/../c").normalize().toString());

      // leading ".." in a relative path is preserved
      assertEquals("../a", new TarPath(fs, false, "../a").normalize().toString());

      // ".." after a preserved ".." is also preserved
      assertEquals("../..", new TarPath(fs, false, "a/../../..").normalize().toString());

      // ".." above root in an absolute path is dropped
      assertEquals("/", new TarPath(fs, true, "a/../..").normalize().toString());
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
