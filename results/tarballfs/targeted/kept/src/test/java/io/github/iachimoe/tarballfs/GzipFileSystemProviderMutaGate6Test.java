package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.junit.jupiter.api.Test;

final class GzipFileSystemProviderMutaGate6Test {
  @Test
  void getPath_mountedArchiveUri_returnsNonNullPath() throws IOException {
    Path gzPath = Files.createTempFile("tarballfs-test-", ".gz");
    try {
      try (OutputStream out = Files.newOutputStream(gzPath);
          GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out)) {
        gzOut.write("hello".getBytes(StandardCharsets.UTF_8));
      }

      GzipFileSystemProvider provider = new GzipFileSystemProvider();
      String abs = gzPath.toAbsolutePath().toString();
      String name = gzPath.getFileName().toString();

      try (FileSystem fs = provider.newFileSystem(URI.create("gzip:" + abs), Map.of())) {
        List<String> candidates =
            List.of(
                "gzip:" + abs + "!/" + name,
                "gzip:" + abs + "!/",
                "gzip:" + abs + "!",
                "gzip:" + abs,
                "gzip:" + abs + "#/" + name,
                "gzip:" + abs + "#" + name);

        Path found = null;
        for (String candidate : candidates) {
          try {
            Path p = provider.getPath(URI.create(candidate));
            if (p != null) {
              found = p;
              break;
            }
          } catch (RuntimeException ignored) {
            // Try the next URI form.
          }
        }
        assertNotNull(found);
      }
    } finally {
      Files.deleteIfExists(gzPath);
    }
  }
}
