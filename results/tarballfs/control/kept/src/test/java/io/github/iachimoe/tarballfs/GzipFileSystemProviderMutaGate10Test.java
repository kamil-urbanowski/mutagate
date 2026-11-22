package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

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

final class GzipFileSystemProviderMutaGate10Test {
  @Test
  void getPath_roundTripsUriOfMountedPath() throws IOException {
    Path gzPath = Files.createTempFile("tarballfs-test-", ".gz");
    try {
      try (OutputStream out = Files.newOutputStream(gzPath);
          GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out)) {
        gzOut.write("hello".getBytes(StandardCharsets.UTF_8));
      }

      GzipFileSystemProvider provider = new GzipFileSystemProvider();
      URI mountUri = URI.create("gzip:" + gzPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(mountUri, Map.of())) {
        Path original = fs.getPath("/", gzPath.getFileName().toString());
        URI uri = original.toUri();

        Path resolved = provider.getPath(uri);

        assertSame(fs, resolved.getFileSystem());
        assertEquals(original.toString(), resolved.toString());
        assertEquals(original, resolved);
      }
    } finally {
      Files.deleteIfExists(gzPath);
    }
  }
}
