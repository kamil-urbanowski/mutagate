package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate12Test {

  @Test
  void endsWithHandlesLongerAndShorterOtherPaths() throws Exception {
    Path tar = Files.createTempFile("tarpath-test", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

      TarPath path = new TarPath(fs, false, "a/b");
      TarPath longer = new TarPath(fs, false, "a/b/c");
      TarPath suffix = new TarPath(fs, false, "b/c");
      TarPath full = new TarPath(fs, false, "a/b/c");

      // other has more segments than this: must be false (not throw)
      assertFalse(path.endsWith(longer));
      // other has fewer segments and matches the tail: must be true
      assertTrue(full.endsWith(suffix));
      // equal length and equal: true
      assertTrue(full.endsWith(longer));
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
