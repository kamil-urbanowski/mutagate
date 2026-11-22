package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

final class GzipFileSystemProviderMutaGate3Test {
  @Test
  void newFileSystem_uri_validatesSchemeNullAndDuplicates() throws IOException {
    Path gzPath = Files.createTempFile("tarballfs-test-", ".gz");
    try {
      byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(gzPath);
          GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out)) {
        gzOut.write(data);
      }

      GzipFileSystemProvider provider = new GzipFileSystemProvider();

      assertThrows(NullPointerException.class, () -> provider.newFileSystem((URI) null, Map.of()));

      URI wrongScheme = URI.create("zip:" + gzPath.toAbsolutePath());
      IllegalArgumentException iae =
          assertThrows(IllegalArgumentException.class, () -> provider.newFileSystem(wrongScheme, Map.of()));
      assertTrue(iae.getMessage().contains("zip"));

      URI uri = URI.create("gzip:" + gzPath.toAbsolutePath());
      FileSystem fs = provider.newFileSystem(uri, Map.of());
      try {
        assertNotNull(fs);
        assertTrue(fs.isOpen());
        assertThrows(FileSystemAlreadyExistsException.class, () -> provider.newFileSystem(uri, Map.of()));
        assertThrows(FileSystemAlreadyExistsException.class, () -> provider.newFileSystem(gzPath, Map.of()));
        assertTrue(fs.isOpen());
      } finally {
        fs.close();
      }
      assertFalse(fs.isOpen());

      try (FileSystem second = provider.newFileSystem(uri, Map.of())) {
        assertTrue(second.isOpen());
        assertEquals("hello", Files.readString(second.getPath("/", gzPath.getFileName().toString())));
      }
    } finally {
      Files.deleteIfExists(gzPath);
    }
  }
}
