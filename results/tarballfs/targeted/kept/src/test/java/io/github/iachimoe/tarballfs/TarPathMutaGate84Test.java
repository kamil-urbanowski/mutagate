package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate84Test {

  @Test
  void subpathBoundaries(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);
    TarPath path = new TarPath(fs, false, "a/b/c");

    // Full range is valid: begin == 0 and end == segments.length
    assertEquals("a/b/c", path.subpath(0, 3).toString());
    assertEquals("a", path.subpath(0, 1).toString());
    assertEquals("c", path.subpath(2, 3).toString());
    assertEquals("b/c", path.subpath(1, 3).toString());

    // Empty range is invalid
    assertThrows(IllegalArgumentException.class, () -> path.subpath(1, 1));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(2, 1));
    // Negative begin is invalid
    assertThrows(IllegalArgumentException.class, () -> path.subpath(-1, 2));
    // End past the last segment is invalid
    assertThrows(IllegalArgumentException.class, () -> path.subpath(0, 4));
  }
}
