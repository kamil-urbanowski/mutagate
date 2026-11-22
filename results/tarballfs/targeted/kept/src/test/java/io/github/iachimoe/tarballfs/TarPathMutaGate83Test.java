package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate83Test {

  @Test
  void startsWithReturnsFalseWhenSegmentsDiffer() throws Exception {
    Path tar = Files.createTempFile("mutagate", ".tar");
    TarFileSystem fs = null;
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      fs = new TarFileSystem(provider, tar.toUri(), tar);

      TarPath path = new TarPath(fs, true, "a/b/c");
      TarPath mismatch = new TarPath(fs, true, "a/x");
      TarPath prefix = new TarPath(fs, true, "a/b");

      assertFalse(path.startsWith(mismatch));
      assertFalse(path.startsWith("x"));
      assertTrue(path.startsWith(prefix));
    } finally {
      if (fs != null) {
        try {
          fs.close();
        } catch (Exception ignored) {
          // ignore
        }
      }
      Files.deleteIfExists(tar);
    }
  }
}
