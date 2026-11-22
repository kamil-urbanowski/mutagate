package io.github.iachimoe.tarballfs;

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

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.Test;

final class GzipFileSystemProviderMutaGate4Test {
  @Test
  void getFileSystem_returnsMountedFileSystem_andThrowsWhenNotMounted() throws IOException {
    Path gzPath = Files.createTempFile("tarballfs-test-", ".gz");
    Path otherPath = Files.createTempFile("tarballfs-test-other-", ".gz");
    try {
      try (OutputStream out = Files.newOutputStream(gzPath);
          GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out)) {
        gzOut.write("hello".getBytes(StandardCharsets.UTF_8));
      }

      GzipFileSystemProvider provider = new GzipFileSystemProvider();
      URI uri = URI.create("gzip:" + gzPath.toAbsolutePath());
      URI otherUri = URI.create("gzip:" + otherPath.toAbsolutePath());

      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        assertSame(fs, provider.getFileSystem(uri));
        assertThrows(FileSystemNotFoundException.class, () -> provider.getFileSystem(otherUri));
      }
    } finally {
      Files.deleteIfExists(gzPath);
      Files.deleteIfExists(otherPath);
    }
  }
}
