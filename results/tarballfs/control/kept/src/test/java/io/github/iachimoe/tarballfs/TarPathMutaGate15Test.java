package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate15Test {

  @TempDir
  Path tempDir;

  @Test
  void subpathBehavior() throws Exception {
    Path tar = tempDir.resolve("archive.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);

    TarPath abs = new TarPath(fs, true, "/a/b/c/d");

    Path middle = abs.subpath(1, 3);
    assertFalse(middle.isAbsolute());
    assertEquals("b/c", middle.toString());
    assertEquals(2, middle.getNameCount());
    assertEquals(new TarPath(fs, false, "b/c"), middle);

    Path full = abs.subpath(0, 4);
    assertFalse(full.isAbsolute());
    assertEquals("a/b/c/d", full.toString());

    assertEquals("c", abs.subpath(2, 3).toString());
    assertEquals("d", abs.subpath(3, 4).toString());

    TarPath rel = new TarPath(fs, false, "x/y/z");
    assertEquals("y/z", rel.subpath(1, 3).toString());
    assertEquals("x", rel.subpath(0, 1).toString());

    assertThrows(IllegalArgumentException.class, () -> abs.subpath(-1, 2));
    assertThrows(IllegalArgumentException.class, () -> abs.subpath(0, 5));
    assertThrows(IllegalArgumentException.class, () -> abs.subpath(2, 2));
    assertThrows(IllegalArgumentException.class, () -> abs.subpath(3, 2));

    TarPath root = new TarPath(fs, true, "");
    assertThrows(IllegalArgumentException.class, () -> root.subpath(0, 0));
    assertThrows(IllegalArgumentException.class, () -> root.subpath(0, 1));
  }
}
