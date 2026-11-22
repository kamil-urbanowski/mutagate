package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class TarPathMutaGate16Test {

  @Test
  void endsWithReturnsFalseForNonTarPath() throws Exception {
    Path tarFile = Files.createTempFile("mutagate16", ".tar");
    try {
      Files.write(tarFile, new byte[1024]);
      URI uri = URI.create("tar:" + tarFile.toAbsolutePath() + "!");
      TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), uri, tarFile);

      TarPath path = new TarPath(fs, false, "a/b");

      Path foreign = Paths.get("b");
      assertFalse(path.endsWith(foreign));

      // sanity check: a TarPath with the same suffix does match
      assertTrue(path.endsWith(new TarPath(fs, false, "b")));
    } finally {
      Files.deleteIfExists(tarFile);
    }
  }
}
