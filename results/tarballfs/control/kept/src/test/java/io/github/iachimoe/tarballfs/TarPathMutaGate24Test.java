package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate24Test {

  @TempDir
  Path tempDir;

  @Test
  void getNameCountCountsNormalizedSegments() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    URI uri = URI.create("tar:" + tar.toAbsolutePath() + "!");
    TarFileSystem fs = new TarFileSystem(provider, uri, tar);

    // Empty / root / null paths have no name elements
    assertEquals(0, new TarPath(fs, false, "").getNameCount());
    assertEquals(0, new TarPath(fs, true, "").getNameCount());
    assertEquals(0, new TarPath(fs, true, "/").getNameCount());
    assertEquals(0, new TarPath(fs, false, null).getNameCount());
    assertEquals(0, new TarPath(fs, false, "///").getNameCount());

    // Simple paths
    assertEquals(1, new TarPath(fs, false, "a").getNameCount());
    assertEquals(3, new TarPath(fs, false, "a/b/c").getNameCount());

    // Absolute flag does not affect the count
    assertEquals(3, new TarPath(fs, true, "a/b/c").getNameCount());

    // Redundant separators are collapsed
    assertEquals(2, new TarPath(fs, false, "a//b").getNameCount());
    assertEquals(2, new TarPath(fs, false, "/a/b/").getNameCount());
    assertEquals(2, new TarPath(fs, true, "//a///b//").getNameCount());

    // Backslashes are treated as separators
    assertEquals(3, new TarPath(fs, false, "a\\b\\c").getNameCount());
    assertEquals(3, new TarPath(fs, false, "a\\b/c").getNameCount());

    // "." and ".." are counted as ordinary segments (no normalization)
    assertEquals(4, new TarPath(fs, false, "a/./b/..").getNameCount());
    assertEquals(1, new TarPath(fs, false, "..").getNameCount());

    // Count of derived paths is consistent
    TarPath p = new TarPath(fs, true, "x/y/z");
    assertEquals(2, p.getParent().getNameCount());
    assertEquals(1, p.getFileName().getNameCount());
    assertEquals(0, p.getRoot().getNameCount());
    assertEquals(2, p.subpath(0, 2).getNameCount());
    assertEquals(3, p.toAbsolutePath().getNameCount());
  }
}
