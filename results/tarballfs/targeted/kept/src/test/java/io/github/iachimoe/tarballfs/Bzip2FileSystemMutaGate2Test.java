package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.Test;

final class Bzip2FileSystemMutaGate2Test {
  @Test
  void bzip2FileStore_returnsNonNullStoreSameAsCompressorFileStore() throws IOException {
    Path bzPath = Files.createTempFile("tarballfs-test-", ".bz2");
    try {
      byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(bzPath);
          BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(out)) {
        bzOut.write(data);
      }

      Bzip2FileSystemProvider provider = new Bzip2FileSystemProvider();
      URI uri = URI.create("bzip2:" + bzPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        Bzip2FileSystem bzFs = (Bzip2FileSystem) fs;
        ReadOnlyCompressorFileStore store = bzFs.bzip2FileStore();
        assertNotNull(store);
        assertSame(bzFs.compressorFileStore(), store);
        assertEquals("bzip2", store.type());
        assertTrue(store.isReadOnly());
      }
    } finally {
      Files.deleteIfExists(bzPath);
    }
  }
}
