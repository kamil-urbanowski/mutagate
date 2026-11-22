package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ReadOnlyCompressorFileStoreMutaGate7Test {

  @Test
  void getUnallocatedSpaceIsZeroRegardlessOfArchiveContent() throws Exception {
    Path dir = Files.createTempDirectory("tarballfs-unalloc");
    Path archive = dir.resolve("data.tar.gz");
    try {
      Files.write(archive, new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

      ReadOnlyCompressorFileStore store = new ReadOnlyCompressorFileStore(archive, "gz");

      assertEquals(0L, store.getUnallocatedSpace());
      assertEquals(10L, store.getTotalSpace());

      Files.write(archive, new byte[2048]);
      assertEquals(0L, store.getUnallocatedSpace());
      assertEquals(0L, store.getUsableSpace());
    } finally {
      Files.deleteIfExists(archive);
      Files.deleteIfExists(dir);
    }
  }
}
