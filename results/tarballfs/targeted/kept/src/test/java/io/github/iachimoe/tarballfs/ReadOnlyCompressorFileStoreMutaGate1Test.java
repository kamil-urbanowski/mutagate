package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class ReadOnlyCompressorFileStoreMutaGate1Test {

  @Test
  void blockSizeIs512() {
    Path archive = Paths.get("archive.tar.gz");
    ReadOnlyCompressorFileStore store = new ReadOnlyCompressorFileStore(archive, "gz");
    assertEquals(512L, store.getBlockSize());
  }
}
