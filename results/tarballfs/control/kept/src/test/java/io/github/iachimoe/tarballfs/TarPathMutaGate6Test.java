package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate6Test {

  @Test
  void iteratorYieldsRelativeNameElementsInOrderAndThenEnds(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("archive.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath path = new TarPath(fs, true, "a/b/c");
    Iterator<Path> it = path.iterator();

    // hasNext is idempotent and does not advance
    assertTrue(it.hasNext());
    assertTrue(it.hasNext());

    List<Path> seen = new ArrayList<>();
    while (it.hasNext()) {
      seen.add(it.next());
    }

    assertEquals(3, seen.size());
    assertEquals(new TarPath(fs, false, "a"), seen.get(0));
    assertEquals(new TarPath(fs, false, "b"), seen.get(1));
    assertEquals(new TarPath(fs, false, "c"), seen.get(2));
    for (Path p : seen) {
      assertFalse(p.isAbsolute());
      assertEquals(1, p.getNameCount());
    }

    assertFalse(it.hasNext());
    assertThrows(IndexOutOfBoundsException.class, it::next);

    // each call to iterator() starts fresh
    assertTrue(path.iterator().hasNext());
    assertEquals(new TarPath(fs, false, "a"), path.iterator().next());

    // root and empty relative paths have no elements
    assertFalse(new TarPath(fs, true, "").iterator().hasNext());
    assertFalse(new TarPath(fs, false, "").iterator().hasNext());
  }
}
