package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate14Test {

  @TempDir
  Path tempDir;

  @Test
  void getFileNameReturnsRelativeLastSegmentOrNull() throws Exception {
    Path tar = tempDir.resolve("test.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

    // Root and empty paths have no file name
    assertNull(new TarPath(fs, true, "").getFileName());
    assertNull(new TarPath(fs, false, "").getFileName());

    // Multi-segment relative path
    Path rel = new TarPath(fs, false, "a/b/c").getFileName();
    assertNotNull(rel);
    assertEquals("c", rel.toString());
    assertFalse(rel.isAbsolute());
    assertEquals(1, rel.getNameCount());
    assertEquals(new TarPath(fs, false, "c"), rel);
    assertSame(fs, rel.getFileSystem());

    // Absolute path yields a relative file name
    Path abs = new TarPath(fs, true, "a/b/dir").getFileName();
    assertNotNull(abs);
    assertEquals("dir", abs.toString());
    assertFalse(abs.isAbsolute());
    assertEquals(new TarPath(fs, false, "dir"), abs);

    // Single segment absolute path
    Path single = new TarPath(fs, true, "only").getFileName();
    assertNotNull(single);
    assertEquals("only", single.toString());
    assertFalse(single.isAbsolute());

    // Trailing slash and backslash separators are normalized
    assertEquals("z", new TarPath(fs, false, "x/y/z/").getFileName().toString());
    assertEquals("q", new TarPath(fs, true, "p\\q").getFileName().toString());

    // Dots are kept as ordinary segment names
    assertEquals("..", new TarPath(fs, false, "a/..").getFileName().toString());
  }
}
