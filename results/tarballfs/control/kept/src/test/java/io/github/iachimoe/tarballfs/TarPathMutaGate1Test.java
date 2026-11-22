package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate1Test {

  @TempDir
  Path tempDir;

  @Test
  void relativizeBehaviors() throws Exception {
    Path tar = tempDir.resolve("a.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);

    TarPath abc = new TarPath(fs, false, "a/b/c");
    TarPath ad = new TarPath(fs, false, "a/d");
    TarPath a = new TarPath(fs, false, "a");

    // diverging branches
    assertEquals("../../d", abc.relativize(ad).toString());
    assertEquals("../b/c", ad.relativize(abc).toString());

    // identical paths -> empty
    assertEquals("", abc.relativize(new TarPath(fs, false, "a/b/c")).toString());
    assertEquals(0, abc.relativize(abc).getNameCount() == 0 ? 0 : 1);

    // descendant and ancestor
    assertEquals("b/c", a.relativize(abc).toString());
    assertEquals("../..", abc.relativize(a).toString());

    // nothing in common
    assertEquals("../../x/y", new TarPath(fs, false, "p/q").relativize(new TarPath(fs, false, "x/y")).toString());

    // empty receiver
    assertEquals("a/b/c", new TarPath(fs, false, "").relativize(abc).toString());

    // absolute paths yield relative result
    TarPath absAB = new TarPath(fs, true, "a/b");
    TarPath absAC = new TarPath(fs, true, "a/c");
    Path rel = absAB.relativize(absAC);
    assertEquals("../c", rel.toString());
    assertFalse(rel.isAbsolute());

    TarPath root = new TarPath(fs, true, "");
    assertEquals("a/b", root.relativize(absAB).toString());
    assertEquals("../..", absAB.relativize(root).toString());

    // mismatched absolute/relative form
    assertThrows(IllegalArgumentException.class, () -> abc.relativize(absAB));
    assertThrows(IllegalArgumentException.class, () -> absAB.relativize(abc));

    // foreign path implementation
    assertThrows(IllegalArgumentException.class, () -> abc.relativize(Paths.get("a/d")));

    // null
    assertThrows(NullPointerException.class, () -> abc.relativize(null));
  }
}
