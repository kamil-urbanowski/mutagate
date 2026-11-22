package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate4Test {

  @Test
  void compareToOrdersPathsByStringForm() throws Exception {
    Path tar = Files.createTempFile("tarpath-compare", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tar.toAbsolutePath().toUri().getPath() + "!");
      TarFileSystem fs = new TarFileSystem(provider, uri, tar);

      TarPath a = new TarPath(fs, false, "a");
      TarPath b = new TarPath(fs, false, "b");
      TarPath aCopy = new TarPath(fs, false, "a");

      assertTrue(a.compareTo(b) < 0);
      assertTrue(b.compareTo(a) > 0);
      assertEquals(0, a.compareTo(aCopy));
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
