package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReadOnlyCompressorFileStoreMutaGate5Test {

  @TempDir
  Path tempDir;

  @Test
  void getTotalSpaceReflectsFileSizeAndFailsForMissingFile() throws IOException {
    Path file = tempDir.resolve("archive.tar.gz");
    Files.write(file, new byte[1234]);

    ReadOnlyCompressorFileStore store = new ReadOnlyCompressorFileStore(file, "gz");
    assertEquals(1234L, store.getTotalSpace());

    Files.write(file, new byte[17]);
    assertEquals(17L, store.getTotalSpace());

    Path missing = tempDir.resolve("missing.tar.gz");
    ReadOnlyCompressorFileStore missingStore = new ReadOnlyCompressorFileStore(missing, "gz");
    assertThrows(IOException.class, missingStore::getTotalSpace);
  }
}
