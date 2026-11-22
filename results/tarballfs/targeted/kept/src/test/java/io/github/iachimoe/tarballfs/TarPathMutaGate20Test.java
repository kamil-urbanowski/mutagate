package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate20Test {

  @Test
  void equalsDistinguishesTarPathsFromOtherObjects(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("archive.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

    TarPath a = new TarPath(fs, true, "dir/file.txt");
    TarPath b = new TarPath(fs, true, "dir/file.txt");
    TarPath different = new TarPath(fs, true, "dir/other.txt");

    assertTrue(a.equals(a));
    assertTrue(a.equals(b));
    assertTrue(b.equals(a));
    assertEquals(a.hashCode(), b.hashCode());
    assertFalse(a.equals(different));
    assertFalse(a.equals("/dir/file.txt"));
    assertFalse(a.equals(null));
  }
}
