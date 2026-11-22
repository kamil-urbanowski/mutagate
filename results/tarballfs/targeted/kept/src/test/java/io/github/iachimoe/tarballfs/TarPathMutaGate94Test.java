package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate94Test {

  @Test
  void iteratorYieldsEachSegmentThenEnds(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("archive.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);
    TarPath path = new TarPath(fs, false, "a/b/c");

    Iterator<Path> it = path.iterator();
    assertTrue(it.hasNext());

    List<String> names = new ArrayList<>();
    while (it.hasNext()) {
      names.add(it.next().toString());
    }

    assertEquals(List.of("a", "b", "c"), names);
    assertFalse(it.hasNext());

    assertFalse(new TarPath(fs, true, "").iterator().hasNext());
  }
}
