package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReadOnlyCompressorFileStoreMutaGate2Test {

  @TempDir
  Path tempDir;

  @Test
  void getTotalSpaceReturnsArchiveFileSize() throws Exception {
    Path archive = tempDir.resolve("archive.tar.gz");
    byte[] content = new byte[1234];
    for (int i = 0; i < content.length; i++) {
      content[i] = (byte) i;
    }
    Files.write(archive, content);

    ReadOnlyCompressorFileStore store = new ReadOnlyCompressorFileStore(archive, "gz");

    assertEquals(1234L, store.getTotalSpace());
  }
}
