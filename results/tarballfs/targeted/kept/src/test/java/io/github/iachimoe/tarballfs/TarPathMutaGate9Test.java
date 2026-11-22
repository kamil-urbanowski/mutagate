package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate9Test {

  @Test
  void endsWithComparesSegmentsFromTheEnd() throws Exception {
    Path tmp = Files.createTempFile("tarpath-endswith", ".tar");
    try {
      Files.write(tmp, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tmp.toAbsolutePath() + "!"), tmp);

      TarPath path = new TarPath(fs, true, "a/b/c");

      assertTrue(path.endsWith(new TarPath(fs, false, "b/c")));
      assertTrue(path.endsWith(new TarPath(fs, false, "c")));
      assertTrue(path.endsWith(new TarPath(fs, false, "a/b/c")));
      assertFalse(path.endsWith(new TarPath(fs, false, "a/c")));
      assertFalse(path.endsWith(new TarPath(fs, false, "b")));
    } finally {
      Files.deleteIfExists(tmp);
    }
  }
}
