package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.Test;

final class GzipFileSystemMutaGate4Test {
  @Test
  void toString_prefixesGzipSchemeAndHostArchivePath() throws IOException {
    Path gzPath = Files.createTempFile("tarballfs-test-", ".gz");
    try {
      try (OutputStream out = Files.newOutputStream(gzPath);
          GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out)) {
        gzOut.write("hello".getBytes(StandardCharsets.UTF_8));
      }

      GzipFileSystemProvider provider = new GzipFileSystemProvider();
      URI uri = URI.create("gzip:" + gzPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        GzipFileSystem gzipFs = (GzipFileSystem) fs;
        String s = gzipFs.toString();
        assertTrue(s.startsWith("gzip:"));
        assertEquals("gzip:" + gzipFs.gzipFile(), s);
      }
    } finally {
      Files.deleteIfExists(gzPath);
    }
  }
}
