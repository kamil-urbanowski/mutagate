package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.Test;

final class GzipFileSystemProviderMutaGate5Test {
  @Test
  void newFileSystem_path_nullRejected_duplicateRejectedWhileOpen_allowedAfterClose() throws IOException {
    GzipFileSystemProvider provider = new GzipFileSystemProvider();
    assertThrows(NullPointerException.class, () -> provider.newFileSystem((Path) null, Map.of()));

    Path gzPath = Files.createTempFile("tarballfs-test-", ".gz");
    try {
      try (OutputStream out = Files.newOutputStream(gzPath);
          GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out)) {
        gzOut.write("hello".getBytes(StandardCharsets.UTF_8));
      }

      FileSystem first = provider.newFileSystem(gzPath, Map.of());
      try {
        assertTrue(first.isOpen());
        assertThrows(FileSystemAlreadyExistsException.class, () -> provider.newFileSystem(gzPath, Map.of()));
        assertTrue(first.isOpen());
      } finally {
        first.close();
      }

      try (FileSystem second = provider.newFileSystem(gzPath, Map.of())) {
        assertNotNull(second);
        assertNotSame(first, second);
        assertTrue(second.isOpen());
      }
    } finally {
      Files.deleteIfExists(gzPath);
    }
  }
}
