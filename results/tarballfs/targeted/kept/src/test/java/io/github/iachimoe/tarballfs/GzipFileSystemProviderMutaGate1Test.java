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
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.Test;

final class GzipFileSystemProviderMutaGate1Test {
  @Test
  void newFileSystem_whileOpenThrowsAlreadyExists_afterCloseCanBeReopened() throws IOException {
    Path gzPath = Files.createTempFile("tarballfs-mutagate-", ".gz");
    try {
      byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(gzPath);
          GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out)) {
        gzOut.write(data);
      }

      GzipFileSystemProvider provider = new GzipFileSystemProvider();
      URI uri = URI.create("gzip:" + gzPath.toAbsolutePath());

      FileSystem first = provider.newFileSystem(uri, Map.of());
      assertNotNull(first);
      assertTrue(first.isOpen());

      assertThrows(FileSystemAlreadyExistsException.class, () -> provider.newFileSystem(uri, Map.of()));
      assertThrows(FileSystemAlreadyExistsException.class, () -> provider.newFileSystem(gzPath, Map.of()));

      first.close();

      try (FileSystem second = provider.newFileSystem(uri, Map.of())) {
        assertNotNull(second);
        assertNotSame(first, second);
        assertTrue(second.isOpen());
      }
    } finally {
      Files.deleteIfExists(gzPath);
    }
  }
}
