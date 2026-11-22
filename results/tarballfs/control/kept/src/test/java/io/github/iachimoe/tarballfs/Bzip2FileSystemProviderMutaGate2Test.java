package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

final class Bzip2FileSystemProviderMutaGate2Test {
  @Test
  void getFileSystem_returnsMountedInstance_andThrowsForUnmountedUri() throws IOException {
    Path bzPath = Files.createTempFile("tarballfs-test-", ".bz2");
    Path other = Files.createTempFile("tarballfs-test-other-", ".bz2");
    Files.delete(other);
    try {
      byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(bzPath);
          BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(out)) {
        bzOut.write(data);
      }

      Bzip2FileSystemProvider provider = new Bzip2FileSystemProvider();
      URI uri = URI.create("bzip2:" + bzPath.toAbsolutePath());

      URI unmounted = URI.create("bzip2:" + other.toAbsolutePath());
      FileSystemNotFoundException before =
          assertThrows(FileSystemNotFoundException.class, () -> provider.getFileSystem(uri));
      assertEquals(uri.toString(), before.getMessage());

      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        assertSame(fs, provider.getFileSystem(uri));
        assertSame(fs, provider.getFileSystem(uri));

        FileSystemNotFoundException ex =
            assertThrows(FileSystemNotFoundException.class, () -> provider.getFileSystem(unmounted));
        assertEquals(unmounted.toString(), ex.getMessage());
      }
    } finally {
      Files.deleteIfExists(bzPath);
      Files.deleteIfExists(other);
    }
  }
}
