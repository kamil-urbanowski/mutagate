package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate28Test {

  @Test
  void getNameCountReturnsNumberOfSegments() throws Exception {
    Path tar = Files.createTempFile("mutagate28", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tar.toAbsolutePath() + "!");
      TarFileSystem fs = new TarFileSystem(provider, uri, tar);

      assertEquals(3, new TarPath(fs, false, "a/b/c").getNameCount());
      assertEquals(2, new TarPath(fs, true, "/x/y/").getNameCount());
      assertEquals(1, new TarPath(fs, false, "single").getNameCount());
      assertEquals(0, new TarPath(fs, true, "").getNameCount());
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
