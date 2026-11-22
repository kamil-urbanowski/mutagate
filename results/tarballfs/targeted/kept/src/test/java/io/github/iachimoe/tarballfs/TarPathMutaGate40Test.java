package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate40Test {

  @Test
  void iteratorReturnsEachNameSegmentInOrder(@TempDir Path dir) throws IOException {
    Path tar = dir.resolve("test.tar");
    Files.write(tar, new byte[1024]);
    URI uri = URI.create("tar:" + tar.toAbsolutePath() + "!");
    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), uri, tar);

    TarPath path = new TarPath(fs, false, "a/b/c");

    Iterator<Path> it = path.iterator();
    assertNotNull(it);

    List<String> names = new ArrayList<>();
    while (it.hasNext()) {
      names.add(it.next().toString());
    }

    assertEquals(List.of("a", "b", "c"), names);
    assertFalse(it.hasNext());
  }
}
