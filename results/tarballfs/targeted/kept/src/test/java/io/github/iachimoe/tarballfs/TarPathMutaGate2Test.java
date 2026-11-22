package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate2Test {

  @TempDir
  Path tempDir;

  @Test
  void trailingSlashesAreStrippedFromEntryName() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

    TarPath single = new TarPath(fs, false, "a/b/");
    assertEquals("a/b", single.entryName());
    assertEquals(2, single.getNameCount());
    assertEquals("a/b", single.toString());

    TarPath multiple = new TarPath(fs, true, "dir//");
    assertEquals("dir", multiple.entryName());
    assertEquals("/dir", multiple.toString());
  }
}
