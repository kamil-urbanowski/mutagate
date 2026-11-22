package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ReadOnlyCompressorFileStoreMutaGate6Test {

  @Test
  void usableSpaceIsZeroEvenWhenArchiveHasContent() throws Exception {
    Path dir = Files.createTempDirectory("rocfs-test");
    Path archive = dir.resolve("data.gz");
    try {
      Files.write(archive, new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

      ReadOnlyCompressorFileStore store = new ReadOnlyCompressorFileStore(archive, "gz");

      assertEquals(10L, store.getTotalSpace());
      assertEquals(0L, store.getUsableSpace());
      assertEquals(0L, store.getUnallocatedSpace());
    } finally {
      Files.deleteIfExists(archive);
      Files.deleteIfExists(dir);
    }
  }
}
