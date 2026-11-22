package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate97Test {

  @TempDir
  Path tempDir;

  @Test
  void iteratorReturnsEachNameInOrder() throws Exception {
    Path tar = tempDir.resolve("archive.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath path = new TarPath(fs, false, "a/b/c");

    Iterator<Path> it = path.iterator();
    List<Path> actual = new ArrayList<>();
    while (it.hasNext()) {
      Path next = it.next();
      assertNotNull(next);
      actual.add(next);
    }
    assertFalse(it.hasNext());

    List<Path> expected = List.of(
        new TarPath(fs, false, "a"),
        new TarPath(fs, false, "b"),
        new TarPath(fs, false, "c"));
    assertEquals(expected, actual);
    assertTrue(actual.get(0).equals(new TarPath(fs, false, "a")));
  }
}
