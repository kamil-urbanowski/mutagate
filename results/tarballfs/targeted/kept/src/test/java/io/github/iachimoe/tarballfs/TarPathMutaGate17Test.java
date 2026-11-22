package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate17Test {

  @Test
  void endsWithReturnsFalseWhenOtherHasMoreSegmentsThanThisPath() throws Exception {
    Path tar = Files.createTempFile("tarpath-test", ".tar");
    try {
      // A valid empty tar archive: two 512-byte zero blocks.
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

      TarPath shorter = new TarPath(fs, false, "b");
      TarPath longer = new TarPath(fs, false, "a/b");

      // The other path has more segments, so this cannot end with it.
      assertFalse(shorter.endsWith(longer));
      assertFalse(shorter.endsWith("a/b"));

      // Sanity checks for the normal behavior.
      assertTrue(longer.endsWith(shorter));
      assertTrue(longer.endsWith(longer));
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
