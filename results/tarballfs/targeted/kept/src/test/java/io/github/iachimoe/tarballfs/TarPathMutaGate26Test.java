package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate26Test {

  @Test
  void equalsDistinguishesDifferentPaths() throws Exception {
    Path tar = Files.createTempFile("tarpath-equals", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

      TarPath a = new TarPath(fs, true, "dir/a.txt");
      TarPath sameAsA = new TarPath(fs, true, "dir/a.txt");
      TarPath otherSegments = new TarPath(fs, true, "dir/b.txt");
      TarPath relative = new TarPath(fs, false, "dir/a.txt");

      assertEquals(a, sameAsA);
      assertNotEquals(a, otherSegments);
      assertNotEquals(a, relative);
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
