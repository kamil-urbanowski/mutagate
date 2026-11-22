package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate65Test {

  @Test
  void resolveRelativePathReturnsCombinedPath() throws Exception {
    Path tar = Files.createTempFile("tarpath-resolve", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tar.toAbsolutePath() + "!");
      TarFileSystem fs = new TarFileSystem(provider, uri, tar);

      TarPath base = new TarPath(fs, true, "a/b");
      Path result = base.resolve(new TarPath(fs, false, "c/d"));

      assertNotNull(result);
      assertEquals(new TarPath(fs, true, "a/b/c/d"), result);
      assertEquals("/a/b/c/d", result.toString());

      TarPath emptyBase = new TarPath(fs, false, "");
      Path result2 = emptyBase.resolve(new TarPath(fs, false, "x"));
      assertNotNull(result2);
      assertEquals(new TarPath(fs, false, "x"), result2);
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
