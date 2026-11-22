package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate19Test {

  @Test
  void equalsDistinguishesSameAndDifferentPaths() throws Exception {
    Path tar = Files.createTempFile("mutagate19", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

      TarPath a = new TarPath(fs, true, "a");
      TarPath aCopy = new TarPath(fs, true, "a");
      TarPath b = new TarPath(fs, true, "b");

      assertTrue(a.equals(a));
      assertTrue(a.equals(aCopy));
      assertEquals(a.hashCode(), aCopy.hashCode());
      assertFalse(a.equals(b));
      assertFalse(b.equals(a));
      assertFalse(a.equals(new TarPath(fs, false, "a")));
      assertFalse(a.equals(null));
      assertFalse(a.equals("a"));
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
