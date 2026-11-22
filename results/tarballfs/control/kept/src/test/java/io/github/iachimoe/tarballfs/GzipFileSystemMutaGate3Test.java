package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.Test;

final class GzipFileSystemMutaGate3Test {
  @Test
  void close_deregistersUri_soItCanBeReopenedAndIsNoLongerLookedUp() throws IOException {
    Path gzPath = Files.createTempFile("tarballfs-test-", ".gz");
    try {
      byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(gzPath);
          GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out)) {
        gzOut.write(data);
      }

      GzipFileSystemProvider provider = new GzipFileSystemProvider();
      URI uri = URI.create("gzip:" + gzPath.toAbsolutePath());

      FileSystem first = provider.newFileSystem(uri, Map.of());
      assertTrue(first.isOpen());
      first.close();
      assertTrue(!first.isOpen());

      assertThrows(FileSystemNotFoundException.class, () -> provider.getFileSystem(uri));

      FileSystem second = provider.newFileSystem(uri, Map.of());
      try {
        assertNotNull(second);
        assertNotSame(first, second);
        assertTrue(second.isOpen());
      } finally {
        second.close();
      }
    } finally {
      Files.deleteIfExists(gzPath);
    }
  }
}
