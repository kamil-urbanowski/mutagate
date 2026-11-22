package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarFileStoreMutaGate4Test {

  @TempDir
  Path tempDir;

  @Test
  void getTotalSpaceReflectsCurrentSizeOfTarFile() throws Exception {
    Path tar = tempDir.resolve("sample.tar");
    Files.write(tar, new byte[1234]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);
    TarFileStore store = new TarFileStore(fs);

    assertEquals(1234L, store.getTotalSpace());

    Files.write(tar, new byte[5000]);
    assertEquals(5000L, store.getTotalSpace());

    Files.write(tar, new byte[0]);
    assertEquals(0L, store.getTotalSpace());
  }
}
