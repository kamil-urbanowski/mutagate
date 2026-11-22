package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class ReadOnlyCompressorFileStoreMutaGate3Test {

  @Test
  void isReadOnlyReturnsTrue() {
    Path archive = Paths.get("archive.tar.gz");
    ReadOnlyCompressorFileStore store = new ReadOnlyCompressorFileStore(archive, "gz");
    assertTrue(store.isReadOnly());
  }
}
