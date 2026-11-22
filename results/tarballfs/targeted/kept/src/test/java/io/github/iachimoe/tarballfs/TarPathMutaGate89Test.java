package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate89Test {

  @TempDir
  Path tempDir;

  @Test
  void subpathValidatesRangeAndReturnsSegments() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);
    TarPath path = new TarPath(fs, true, "a/b/c");

    assertEquals("a/b/c", path.subpath(0, 3).toString());
    assertEquals("a", path.subpath(0, 1).toString());
    assertEquals("b", path.subpath(1, 2).toString());
    assertEquals("b/c", path.subpath(1, 3).toString());

    assertThrows(IllegalArgumentException.class, () -> path.subpath(-1, 2));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(0, 4));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(2, 2));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(2, 1));
  }
}
