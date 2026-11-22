package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarFileStoreMutaGate2Test {

  @Test
  void getTotalSpaceReturnsSizeOfTarFile(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("sample.tar");
    byte[] content = new byte[1024];
    Files.write(tar, content);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);
    TarFileStore store = new TarFileStore(fs);

    assertEquals(1024L, store.getTotalSpace());
  }
}
