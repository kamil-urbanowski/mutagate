package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate22Test {

  @Test
  void equalsComparesAbsoluteFileSystemAndSegments() throws IOException {
    Path tar = Files.createTempFile("tarpath-equals", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tar.toAbsolutePath() + "!");
      TarFileSystem fs1 = new TarFileSystem(provider, uri, tar);
      TarFileSystem fs2 = new TarFileSystem(provider, uri, tar);

      TarPath base = new TarPath(fs1, true, "a/b");
      TarPath same = new TarPath(fs1, true, "/a/b/");
      assertTrue(base.equals(same));
      assertEquals(base.hashCode(), same.hashCode());

      assertNotEquals(base, new TarPath(fs1, false, "a/b"));
      assertNotEquals(base, new TarPath(fs2, true, "a/b"));
      assertNotEquals(base, new TarPath(fs1, true, "a/c"));
      assertNotEquals(base, new TarPath(fs1, true, "a"));
      assertNotEquals(base, "a/b");
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
