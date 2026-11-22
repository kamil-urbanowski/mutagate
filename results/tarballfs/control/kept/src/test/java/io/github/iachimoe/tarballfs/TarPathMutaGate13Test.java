package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate13Test {

  @TempDir
  Path tempDir;

  @Test
  void resolveSiblingCoversParentAndNoParentCases() throws Exception {
    Path tar = tempDir.resolve("test.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    // Relative path with a parent: sibling replaces last segment
    TarPath relative = new TarPath(fs, false, "a/b");
    assertEquals(new TarPath(fs, false, "a/c"), relative.resolveSibling(new TarPath(fs, false, "c")));
    assertEquals(new TarPath(fs, false, "a/x/y"), relative.resolveSibling(new TarPath(fs, false, "x/y")));

    // Absolute path with a parent
    TarPath absolute = new TarPath(fs, true, "a/b/c");
    assertEquals(new TarPath(fs, true, "a/b/d"), absolute.resolveSibling(new TarPath(fs, false, "d")));

    // Absolute single segment: parent is root
    TarPath absSingle = new TarPath(fs, true, "a");
    assertEquals(new TarPath(fs, true, "c"), absSingle.resolveSibling(new TarPath(fs, false, "c")));

    // Relative single segment: no parent, so resolves other against itself
    TarPath relSingle = new TarPath(fs, false, "a");
    assertEquals("a/c", relSingle.resolveSibling(new TarPath(fs, false, "c")).toString());

    // Root: no parent
    TarPath root = new TarPath(fs, true, "");
    assertEquals(new TarPath(fs, true, "c"), root.resolveSibling(new TarPath(fs, false, "c")));

    // Absolute other is returned as is
    TarPath otherAbs = new TarPath(fs, true, "z");
    assertSame(otherAbs, relative.resolveSibling(otherAbs));
    assertSame(otherAbs, relSingle.resolveSibling(otherAbs));
  }
}
