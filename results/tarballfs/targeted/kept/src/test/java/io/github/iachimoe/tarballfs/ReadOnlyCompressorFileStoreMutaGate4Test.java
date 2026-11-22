package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class ReadOnlyCompressorFileStoreMutaGate4Test {

  @Test
  void nameReturnsArchiveFileName() {
    Path archive = Paths.get("some", "dir", "archive.tar.gz");
    ReadOnlyCompressorFileStore store = new ReadOnlyCompressorFileStore(archive, "gz");

    assertEquals("archive.tar.gz", store.name());
  }
}
