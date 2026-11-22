package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReadOnlyCompressorFileStoreMutaGate1Test {

  @TempDir
  Path tempDir;

  @Test
  void constructorStoresArchiveFileAndType() throws Exception {
    Path archive = tempDir.resolve("sample.tar.gz");
    byte[] data = "hello archive content".getBytes(StandardCharsets.UTF_8);
    Files.write(archive, data);

    ReadOnlyCompressorFileStore store = new ReadOnlyCompressorFileStore(archive, "gzip");

    assertEquals("sample.tar.gz", store.name());
    assertEquals("gzip", store.type());
    assertEquals((long) data.length, store.getTotalSpace());
    assertTrue(store.isReadOnly());
  }
}
