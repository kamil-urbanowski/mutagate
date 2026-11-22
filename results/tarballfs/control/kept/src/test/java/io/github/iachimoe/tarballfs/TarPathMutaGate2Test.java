package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate2Test {

  private static String norm(TarFileSystem fs, boolean absolute, String name) {
    return new TarPath(fs, absolute, name).normalize().toString();
  }

  @Test
  void normalizeHandlesDotAndDotDotSegments() throws Exception {
    Path tar = Files.createTempFile("tarpath-normalize", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystem fs = new TarFileSystem(
          new TarFileSystemProvider(), tar.toUri(), tar);

      // relative paths
      assertEquals("a/c", norm(fs, false, "a/./b/../c"));
      assertEquals("../../a", norm(fs, false, "../../a"));
      assertEquals("..", norm(fs, false, "a/../.."));
      assertEquals("..", norm(fs, false, "../a/.."));
      assertEquals("../b", norm(fs, false, "a/../../b"));
      assertEquals("../..", norm(fs, false, "../.."));
      assertEquals("", norm(fs, false, "a/.."));
      assertEquals("", norm(fs, false, "."));

      // absolute paths: ".." above the root is dropped
      assertEquals("/a", norm(fs, true, "../a"));
      assertEquals("/", norm(fs, true, "a/../.."));
      assertEquals("/b", norm(fs, true, "./a/../b/."));

      // absolute flag is preserved
      assertTrue(new TarPath(fs, true, "a/./b").normalize().isAbsolute());
      assertFalse(new TarPath(fs, false, "a/./b").normalize().isAbsolute());

      // result belongs to the same file system
      assertEquals(fs, new TarPath(fs, false, "a/.").normalize().getFileSystem());
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
