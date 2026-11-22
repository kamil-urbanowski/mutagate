package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate23Test {

  @Test
  void equalsComparesSegments() throws Exception {
    Path tar = Files.createTempFile("mutagate23", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

      TarPath a = new TarPath(fs, true, "dir/file.txt");
      TarPath sameAsA = new TarPath(fs, true, "dir/file.txt");
      TarPath different = new TarPath(fs, true, "dir/other.txt");

      assertEquals(a, sameAsA);
      assertEquals(a.hashCode(), sameAsA.hashCode());
      assertNotEquals(a, different);
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
