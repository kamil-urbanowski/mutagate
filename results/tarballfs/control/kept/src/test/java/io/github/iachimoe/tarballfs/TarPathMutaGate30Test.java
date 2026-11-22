package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate30Test {

  @TempDir
  Path tempDir;

  @Test
  void entryNameNormalizesSeparatorsAndIgnoresAbsoluteFlag() throws Exception {
    Path tar = tempDir.resolve("archive.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

    // Leading/trailing/duplicate slashes are collapsed, and no leading slash is kept.
    assertEquals("a/b/c", new TarPath(fs, true, "/a//b/c/").entryName());
    assertEquals("a/b/c", new TarPath(fs, false, "a/b/c").entryName());

    // Backslashes are treated as separators.
    assertEquals("dir/sub/file.txt", new TarPath(fs, false, "dir\\sub\\file.txt").entryName());
    assertEquals("x/y", new TarPath(fs, true, "\\x\\\\y\\").entryName());

    // Single segment.
    assertEquals("single", new TarPath(fs, true, "/single").entryName());

    // Empty, null, and root-only inputs all yield the empty string.
    assertEquals("", new TarPath(fs, true, "").entryName());
    assertEquals("", new TarPath(fs, false, null).entryName());
    assertEquals("", new TarPath(fs, true, "///").entryName());

    // "." and ".." segments are preserved (no normalization here).
    assertEquals("a/./../b", new TarPath(fs, false, "a/./../b").entryName());

    // Derived paths yield the expected entry names.
    TarPath p = new TarPath(fs, true, "/one/two/three");
    assertEquals("one/two", ((TarPath) p.getParent()).entryName());
    assertEquals("three", ((TarPath) p.getFileName()).entryName());
    assertEquals("two/three", ((TarPath) p.subpath(1, 3)).entryName());
    assertEquals("one/two/three/four", ((TarPath) p.resolve("four")).entryName());
  }
}
