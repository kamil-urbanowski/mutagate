package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate12Test {

  @TempDir
  Path tempDir;

  @Test
  void getParentBehavior() throws Exception {
    Path tar = tempDir.resolve("test.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystem fs = new TarFileSystem(
        new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    // absolute multi-segment: parent drops last segment and stays absolute
    Path abs = new TarPath(fs, true, "a/b/c");
    Path absParent = abs.getParent();
    assertNotNull(absParent);
    assertTrue(absParent.isAbsolute());
    assertEquals(new TarPath(fs, true, "a/b"), absParent);
    assertEquals("/a/b", absParent.toString());
    assertEquals(2, absParent.getNameCount());

    // relative multi-segment: parent stays relative
    Path rel = new TarPath(fs, false, "a/b/c");
    Path relParent = rel.getParent();
    assertNotNull(relParent);
    assertFalse(relParent.isAbsolute());
    assertEquals(new TarPath(fs, false, "a/b"), relParent);
    assertEquals("a/b", relParent.toString());

    // two segments
    assertEquals(new TarPath(fs, false, "a"), new TarPath(fs, false, "a/b").getParent());
    assertEquals("/a", new TarPath(fs, true, "a/b").getParent().toString());

    // single segment: absolute -> root, relative -> null
    Path rootParent = new TarPath(fs, true, "a").getParent();
    assertNotNull(rootParent);
    assertTrue(rootParent.isAbsolute());
    assertEquals(0, rootParent.getNameCount());
    assertEquals("/", rootParent.toString());
    assertNull(new TarPath(fs, false, "a").getParent());

    // no segments: null for both root and empty relative path
    assertNull(new TarPath(fs, true, "").getParent());
    assertNull(new TarPath(fs, false, "").getParent());

    // repeated getParent walks up to root, then null
    Path p = new TarPath(fs, true, "x/y/z");
    assertEquals("/x/y", p.getParent().toString());
    assertEquals("/x", p.getParent().getParent().toString());
    assertEquals("/", p.getParent().getParent().getParent().toString());
    assertNull(p.getParent().getParent().getParent().getParent());
  }
}
