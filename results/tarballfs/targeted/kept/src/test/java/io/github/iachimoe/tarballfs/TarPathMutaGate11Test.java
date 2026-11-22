package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate11Test {

  @Test
  void endsWithDistinguishesTarPathFromForeignPath() throws Exception {
    Path tar = Files.createTempFile("mutagate11", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystem fs = new TarFileSystem(
          new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

      TarPath path = new TarPath(fs, true, "a/b/c");

      assertTrue(path.endsWith(new TarPath(fs, false, "b/c")));
      assertFalse(path.endsWith(new TarPath(fs, false, "x/c")));
      assertFalse(path.endsWith(Path.of("b/c")));
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
