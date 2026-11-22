package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate26Test {

  @Test
  void resolveStringBehaviors() throws Exception {
    Path tar = Files.createTempFile("tarpath-resolve", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

      TarPath relBase = new TarPath(fs, false, "a/b");
      Path r1 = relBase.resolve("c/d");
      assertEquals("a/b/c/d", r1.toString());
      assertFalse(r1.isAbsolute());
      assertEquals(4, r1.getNameCount());
      assertSame(fs, r1.getFileSystem());

      TarPath absBase = new TarPath(fs, true, "a");
      Path r2 = absBase.resolve("c");
      assertEquals("/a/c", r2.toString());
      assertTrue(r2.isAbsolute());

      // leading slash in the string is stripped, treated as relative
      assertEquals("/a/c", absBase.resolve("/c").toString());

      // empty base
      assertEquals("x", new TarPath(fs, false, "").resolve("x").toString());
      assertEquals("/x", new TarPath(fs, true, "").resolve("x").toString());

      // empty other
      assertEquals("a/b", relBase.resolve("").toString());
      assertEquals("/a", absBase.resolve("").toString());

      // backslashes are normalized
      assertEquals("a/b/c/d", relBase.resolve("c\\d").toString());

      // equals to the Path-based resolve
      assertEquals(relBase.resolve(new TarPath(fs, false, "c/d")), r1);
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
