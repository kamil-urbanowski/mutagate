package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarFileStoreMutaGate1Test {

  @TempDir
  Path tempDir;

  @Test
  void nameReturnsOnlyTheFileNameOfTheTarFile() throws Exception {
    Path dir = Files.createDirectory(tempDir.resolve("some-parent-dir"));
    Path tar = dir.resolve("my archive.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);
    TarFileStore store = new TarFileStore(fs);

    String name = store.name();

    assertEquals("my archive.tar", name);
    assertNotEquals(tar.toString(), name);
    assertEquals(tar.getFileName().toString(), name);
  }
}
