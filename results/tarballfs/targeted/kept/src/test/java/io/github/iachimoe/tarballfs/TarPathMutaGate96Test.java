package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class TarPathMutaGate96Test {

  @Test
  void iteratorReturnsEachSegmentInOrder() throws Exception {
    Path tar = Files.createTempFile("mutagate96", ".tar");
    try {
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);
      TarPath path = new TarPath(fs, false, "a/b/c");

      List<String> names = new ArrayList<>();
      for (Path p : path) {
        names.add(p.toString());
      }

      assertEquals(List.of("a", "b", "c"), names);
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
