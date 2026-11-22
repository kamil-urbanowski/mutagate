package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate32Test {

  @Test
  void getParentOfSingleSegmentPathDependsOnAbsoluteness(@TempDir Path dir) throws IOException {
    Path tar = dir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);

    Path absoluteParent = new TarPath(fs, true, "a").getParent();
    assertNotNull(absoluteParent);
    assertTrue(absoluteParent.isAbsolute());
    assertEquals("/", absoluteParent.toString());
    assertEquals(0, absoluteParent.getNameCount());

    assertNull(new TarPath(fs, false, "a").getParent());
  }
}
