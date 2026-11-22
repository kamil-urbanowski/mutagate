package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate71Test {

  @TempDir
  Path tempDir;

  @Test
  void startsWithStringReflectsPrefixMatch() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[10240]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);
    try {
      TarPath path = new TarPath(fs, false, "a/b/c");

      assertTrue(path.startsWith("a/b"));
      assertFalse(path.startsWith("x"));
      assertFalse(path.startsWith("a/c"));
    } finally {
      fs.close();
    }
  }
}
