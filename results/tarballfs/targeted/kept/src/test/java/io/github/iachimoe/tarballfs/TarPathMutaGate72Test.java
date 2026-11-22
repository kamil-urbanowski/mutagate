package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate72Test {

  @Test
  void startsWithIsTrueForPathOfEqualLength() throws Exception {
    Path tar = Files.createTempFile("tarpath-test", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);

      TarPath path = new TarPath(fs, true, "a/b");
      TarPath same = new TarPath(fs, true, "a/b");
      TarPath longer = new TarPath(fs, true, "a/b/c");

      // Equal segment counts: must not be rejected by the length check
      assertTrue(path.startsWith(same));
      // Longer prefix than the path itself: must be rejected
      assertFalse(path.startsWith(longer));
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
