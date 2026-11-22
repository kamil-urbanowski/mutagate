package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate5Test {

  @TempDir
  Path tempDir;

  private TarFileSystem newFs(String name) throws IOException {
    Path tar = tempDir.resolve(name);
    Files.write(tar, new byte[1024]);
    return new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);
  }

  @Test
  void resolveBehaviors() throws IOException {
    TarFileSystem fs = newFs("one.tar");
    TarFileSystem other = newFs("two.tar");

    // absolute base + relative other keeps absolute form and joins segments
    TarPath absBase = new TarPath(fs, true, "a/b");
    Path r1 = absBase.resolve(new TarPath(fs, false, "c/d"));
    assertEquals(new TarPath(fs, true, "a/b/c/d"), r1);
    assertTrue(r1.isAbsolute());
    assertEquals("/a/b/c/d", r1.toString());

    // relative base stays relative
    TarPath relBase = new TarPath(fs, false, "a");
    Path r2 = relBase.resolve(new TarPath(fs, false, "b"));
    assertFalse(r2.isAbsolute());
    assertEquals("a/b", r2.toString());

    // root base resolves to just the other entry, no leading duplicate separators
    TarPath root = new TarPath(fs, true, "");
    Path r3 = root.resolve(new TarPath(fs, false, "x"));
    assertEquals("/x", r3.toString());

    // empty other yields an equal path to base
    assertEquals(absBase, absBase.resolve(new TarPath(fs, false, "")));

    // empty base and empty other
    assertEquals(new TarPath(fs, false, ""), new TarPath(fs, false, "").resolve(new TarPath(fs, false, "")));

    // absolute other is returned as-is, even from a different file system
    TarPath absOther = new TarPath(fs, true, "z");
    assertSame(absOther, relBase.resolve(absOther));
    TarPath foreignAbs = new TarPath(other, true, "q");
    assertSame(foreignAbs, absBase.resolve(foreignAbs));

    // string overload
    assertEquals(new TarPath(fs, true, "a/b/e/f"), absBase.resolve("e/f"));

    // relative path from a different file system is rejected
    assertThrows(IllegalArgumentException.class,
        () -> absBase.resolve(new TarPath(other, false, "c")));

    // relative non-TarPath is rejected
    assertThrows(IllegalArgumentException.class, () -> absBase.resolve(Path.of("rel")));

    // null is rejected
    assertThrows(NullPointerException.class, () -> absBase.resolve((Path) null));
  }
}
