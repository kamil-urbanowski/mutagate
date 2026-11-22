package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate27Test {

  @TempDir
  Path tempDir;

  @Test
  void resolveSiblingWithStringResolvesAgainstParent() throws Exception {
    Path tar = Files.createFile(tempDir.resolve("test.tar"));
    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    // absolute, multiple segments: sibling replaces last segment
    assertEquals("/a/c", new TarPath(fs, true, "a/b").resolveSibling("c").toString());
    // relative, multiple segments
    assertEquals("a/c", new TarPath(fs, false, "a/b").resolveSibling("c").toString());
    // multi-segment sibling string
    assertEquals("/a/x/y", new TarPath(fs, true, "a/b").resolveSibling("x/y").toString());
    // leading slash in the string is treated as relative
    assertEquals("/a/x", new TarPath(fs, true, "a/b").resolveSibling("/x").toString());
    // absolute single segment: parent is root
    assertEquals("/c", new TarPath(fs, true, "a").resolveSibling("c").toString());
    // relative single segment: no parent, so resolves against itself
    assertEquals("a/c", new TarPath(fs, false, "a").resolveSibling("c").toString());
    // root has no parent
    assertEquals("/c", new TarPath(fs, true, "").resolveSibling("c").toString());
    // empty string sibling yields the parent
    assertEquals("/a", new TarPath(fs, true, "a/b").resolveSibling("").toString());
    // result equals the equivalent path built directly
    assertEquals(new TarPath(fs, true, "a/c"), new TarPath(fs, true, "a/b").resolveSibling("c"));
  }
}
