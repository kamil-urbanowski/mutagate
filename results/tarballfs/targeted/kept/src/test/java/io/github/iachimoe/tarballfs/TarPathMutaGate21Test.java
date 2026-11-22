package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate21Test {

  @TempDir
  Path tempDir;

  @Test
  void equalsComparesAbsoluteFileSystemAndSegments() throws Exception {
    Path tar1 = tempDir.resolve("one.tar");
    Path tar2 = tempDir.resolve("two.tar");
    Files.write(tar1, new byte[1024]);
    Files.write(tar2, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs1 = new TarFileSystem(provider, tar1.toUri(), tar1);
    TarFileSystem fs2 = new TarFileSystem(provider, tar2.toUri(), tar2);

    TarPath base = new TarPath(fs1, true, "a/b");
    TarPath same = new TarPath(fs1, true, "a/b");
    TarPath relative = new TarPath(fs1, false, "a/b");
    TarPath otherFs = new TarPath(fs2, true, "a/b");
    TarPath otherSegments = new TarPath(fs1, true, "a/c");

    assertTrue(base.equals(same));
    assertEquals(base.hashCode(), same.hashCode());
    assertFalse(base.equals(relative));
    assertFalse(base.equals(otherFs));
    assertFalse(base.equals(otherSegments));
    assertTrue(relative.equals(new TarPath(fs1, false, "a/b")));
    assertFalse(base.equals("a/b"));
  }
}
