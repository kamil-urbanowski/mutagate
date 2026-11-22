package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate6Test {

  @TempDir
  Path tempDir;

  @Test
  void endsWithStringReflectsActualSuffix() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);
    TarPath path = new TarPath(fs, false, "a/b/c");

    assertTrue(path.endsWith("c"));
    assertTrue(path.endsWith("b/c"));
    assertFalse(path.endsWith("b"));
    assertFalse(path.endsWith("x/c"));
  }
}
