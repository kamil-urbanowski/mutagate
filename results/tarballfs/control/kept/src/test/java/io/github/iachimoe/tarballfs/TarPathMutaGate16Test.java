package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate16Test {

  @TempDir
  Path tempDir;

  @Test
  void toStringFormatsAbsoluteRelativeAndNormalizedPaths() throws Exception {
    Path tar = tempDir.resolve("test.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    // Empty paths
    assertEquals("", new TarPath(fs, false, "").toString());
    assertEquals("", new TarPath(fs, false, null).toString());
    assertEquals("/", new TarPath(fs, true, "").toString());
    assertEquals("/", new TarPath(fs, true, "///").toString());

    // Simple paths
    assertEquals("a", new TarPath(fs, false, "a").toString());
    assertEquals("/a", new TarPath(fs, true, "a").toString());
    assertEquals("a/b/c", new TarPath(fs, false, "a/b/c").toString());
    assertEquals("/a/b/c", new TarPath(fs, true, "a/b/c").toString());

    // Normalization of separators in the constructor
    assertEquals("a/b", new TarPath(fs, false, "/a/b/").toString());
    assertEquals("/a/b", new TarPath(fs, true, "/a/b/").toString());
    assertEquals("a/b", new TarPath(fs, false, "a\\b").toString());
    assertEquals("a/b/c", new TarPath(fs, false, "a//b///c").toString());
    assertEquals("/a/b", new TarPath(fs, true, "\\a\\b\\").toString());

    // Derived paths
    TarPath p = new TarPath(fs, false, "x/y/z");
    assertEquals("/x/y/z", p.toAbsolutePath().toString());
    assertEquals("x/y", p.getParent().toString());
    assertEquals("z", p.getFileName().toString());
    assertEquals("y/z", p.subpath(1, 3).toString());
    assertEquals("/", p.toAbsolutePath().getRoot().toString());
    assertEquals("", p.relativize(p).toString());
  }
}
