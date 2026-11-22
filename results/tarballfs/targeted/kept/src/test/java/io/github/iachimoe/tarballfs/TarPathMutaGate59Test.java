package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate59Test {

  @TempDir
  Path tempDir;

  @Test
  void resolveStringReturnsCombinedPath() throws Exception {
    Path tar = tempDir.resolve("test.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);
    TarPath base = new TarPath(fs, true, "a");

    Path resolved = base.resolve("b/c");

    assertNotNull(resolved);
    assertEquals("/a/b/c", resolved.toString());
    assertEquals(new TarPath(fs, true, "a/b/c"), resolved);
  }
}
