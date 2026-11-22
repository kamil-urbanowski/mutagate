package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class ReadOnlyCompressorFileStoreMutaGate2Test {

  @Test
  void nameReturnsOnlyFileNameWithoutParentDirectories() {
    Path archive = Paths.get("some", "nested", "dir", "archive.tar.gz");
    ReadOnlyCompressorFileStore store = new ReadOnlyCompressorFileStore(archive, "gz");

    assertEquals("archive.tar.gz", store.name());

    Path plain = Paths.get("single.bz2");
    ReadOnlyCompressorFileStore plainStore = new ReadOnlyCompressorFileStore(plain, "bz2");
    assertEquals("single.bz2", plainStore.name());
  }
}
