package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate33Test {

  @TempDir
  Path tempDir;

  @Test
  void getParentOfMultiSegmentPathReturnsPathWithoutLastSegment() throws Exception {
    Path tar = tempDir.resolve("archive.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);

    Path absParent = new TarPath(fs, true, "a/b/c").getParent();
    assertNotNull(absParent);
    assertEquals(new TarPath(fs, true, "a/b"), absParent);
    assertEquals("/a/b", absParent.toString());

    Path relParent = new TarPath(fs, false, "x/y").getParent();
    assertNotNull(relParent);
    assertEquals(new TarPath(fs, false, "x"), relParent);
    assertEquals("x", relParent.toString());
  }
}
