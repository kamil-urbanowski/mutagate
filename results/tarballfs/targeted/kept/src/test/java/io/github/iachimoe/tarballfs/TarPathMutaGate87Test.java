package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate87Test {

  @TempDir
  Path tempDir;

  @Test
  void subpathAcceptsValidRangesAndRejectsInvalidOnes() throws Exception {
    Path tar = tempDir.resolve("archive.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystem fs = new TarFileSystem(
        new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath path = new TarPath(fs, true, "a/b/c");

    Path full = path.subpath(0, 3);
    assertEquals("a/b/c", full.toString());
    assertFalse(full.isAbsolute());
    assertEquals("b", path.subpath(1, 2).toString());
    assertEquals("a/b", path.subpath(0, 2).toString());
    assertEquals("b/c", path.subpath(1, 3).toString());

    assertThrows(IllegalArgumentException.class, () -> path.subpath(-1, 2));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(0, 4));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(1, 1));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(2, 1));
  }
}
