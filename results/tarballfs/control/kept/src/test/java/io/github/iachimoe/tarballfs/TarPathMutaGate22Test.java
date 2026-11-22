package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate22Test {

  @Test
  void isAbsoluteReflectsFlagAndIsPropagatedByDerivedPaths(@TempDir Path tmp) throws Exception {
    Path tar = Files.createFile(tmp.resolve("archive.tar"));
    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), URI.create("tar:" + tar.toUri()), tar);

    TarPath abs = new TarPath(fs, true, "a/b/c");
    TarPath rel = new TarPath(fs, false, "a/b/c");

    assertTrue(abs.isAbsolute());
    assertFalse(rel.isAbsolute());

    // the flag governs, not a leading slash in the entry name
    assertFalse(new TarPath(fs, false, "/a/b").isAbsolute());
    assertTrue(new TarPath(fs, true, "a/b").isAbsolute());

    // empty paths
    assertTrue(new TarPath(fs, true, "").isAbsolute());
    assertFalse(new TarPath(fs, false, "").isAbsolute());

    // root / parent / file name / names
    assertTrue(abs.getRoot().isAbsolute());
    assertNull(rel.getRoot());
    assertFalse(abs.getFileName().isAbsolute());
    assertTrue(abs.getParent().isAbsolute());
    assertFalse(rel.getParent().isAbsolute());
    assertTrue(new TarPath(fs, true, "a").getParent().isAbsolute());
    assertNull(new TarPath(fs, false, "a").getParent());
    assertFalse(abs.getName(0).isAbsolute());
    assertFalse(abs.subpath(0, 2).isAbsolute());

    // toAbsolutePath
    assertTrue(rel.toAbsolutePath().isAbsolute());
    assertSame(abs, abs.toAbsolutePath());

    // normalize preserves the flag
    assertTrue(new TarPath(fs, true, "a/../b").normalize().isAbsolute());
    assertTrue(new TarPath(fs, true, "..").normalize().isAbsolute());
    assertFalse(new TarPath(fs, false, "a/../b").normalize().isAbsolute());

    // resolve
    assertTrue(abs.resolve("d").isAbsolute());
    assertFalse(rel.resolve("d").isAbsolute());
    assertSame(abs, rel.resolve(abs));
    assertTrue(rel.resolve(abs).isAbsolute());

    // resolveSibling
    assertTrue(abs.resolveSibling("z").isAbsolute());
    assertFalse(rel.resolveSibling("z").isAbsolute());

    // relativize always yields a relative path
    assertFalse(abs.relativize(new TarPath(fs, true, "a/x")).isAbsolute());
    assertFalse(rel.relativize(new TarPath(fs, false, "a/x")).isAbsolute());

    // iterator names are relative
    for (Path p : abs) {
      assertFalse(p.isAbsolute());
    }
  }
}
