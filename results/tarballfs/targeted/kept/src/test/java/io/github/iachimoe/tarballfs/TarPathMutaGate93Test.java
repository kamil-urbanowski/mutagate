package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate93Test {

  @TempDir
  Path tempDir;

  @Test
  void iteratorStopsExactlyAfterLastSegment() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystem fs = new TarFileSystem(
        new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath path = new TarPath(fs, false, "a/b/c");
    Iterator<Path> it = path.iterator();

    List<String> names = new ArrayList<>();
    for (int n = 0; n < 3; n++) {
      assertTrue(it.hasNext());
      names.add(it.next().toString());
    }
    assertEquals(List.of("a", "b", "c"), names);
    assertFalse(it.hasNext());

    TarPath root = new TarPath(fs, true, "");
    assertFalse(root.iterator().hasNext());
  }
}
