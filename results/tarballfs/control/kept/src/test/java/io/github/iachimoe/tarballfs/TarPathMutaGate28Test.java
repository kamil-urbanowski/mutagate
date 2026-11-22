package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate28Test {

  @Test
  void toAbsolutePathBehavior(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("test.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

    // Relative path becomes absolute, keeping segments and file system
    TarPath relative = new TarPath(fs, false, "a/b/c");
    Path abs = relative.toAbsolutePath();
    assertNotSame(relative, abs);
    assertTrue(abs.isAbsolute());
    assertFalse(relative.isAbsolute());
    assertEquals("/a/b/c", abs.toString());
    assertEquals("a/b/c", relative.toString());
    assertEquals(3, abs.getNameCount());
    assertSame(fs, abs.getFileSystem());
    assertEquals(new TarPath(fs, true, "a/b/c"), abs);
    assertEquals(new TarPath(fs, true, "").toString(), abs.getRoot().toString());

    // Already absolute path returns the same instance
    TarPath absolute = new TarPath(fs, true, "x/y");
    assertSame(absolute, absolute.toAbsolutePath());

    // Idempotent
    assertSame(abs, abs.toAbsolutePath());

    // Empty relative path becomes root
    Path emptyAbs = new TarPath(fs, false, "").toAbsolutePath();
    assertTrue(emptyAbs.isAbsolute());
    assertEquals("/", emptyAbs.toString());
    assertEquals(0, emptyAbs.getNameCount());
  }
}
