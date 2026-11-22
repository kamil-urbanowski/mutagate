package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.Test;

final class Bzip2FileSystemMutaGate3Test {
  @Test
  void close_deregistersFileSystemFromProvider() throws IOException {
    Path bzPath = Files.createTempFile("tarballfs-test-", ".bz2");
    try {
      byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(bzPath);
          BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(out)) {
        bzOut.write(data);
      }

      Bzip2FileSystemProvider provider = new Bzip2FileSystemProvider();
      URI uri = URI.create("bzip2:" + bzPath.toAbsolutePath());

      FileSystem fs = provider.newFileSystem(uri, Map.of());
      assertNotNull(fs);
      fs.close();

      assertThrows(FileSystemNotFoundException.class, () -> provider.getFileSystem(uri));

      try (FileSystem again = provider.newFileSystem(uri, Map.of())) {
        assertNotNull(again);
        assertTrue(again.isOpen());
      }
    } finally {
      Files.deleteIfExists(bzPath);
    }
  }
}
