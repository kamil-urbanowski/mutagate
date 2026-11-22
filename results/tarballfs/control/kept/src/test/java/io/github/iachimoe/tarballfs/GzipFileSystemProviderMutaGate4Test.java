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

final class GzipFileSystemProviderMutaGate4Test {
  @Test
  void readAttributes_stringForm_returnsRequestedBasicAttributes() throws IOException {
    Path gzPath = Files.createTempFile("tarballfs-test-", ".gz");
    try {
      byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(gzPath);
          GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(out)) {
        gzOut.write(data);
      }

      GzipFileSystemProvider provider = new GzipFileSystemProvider();
      URI uri = URI.create("gzip:" + gzPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        Path file = fs.getPath("/", gzPath.getFileName().toString());
        Map<String, Object> fileAttrs = provider.readAttributes(file, "basic:size,isRegularFile");
        assertEquals(5L, fileAttrs.get("size"));
        assertEquals(Boolean.TRUE, fileAttrs.get("isRegularFile"));

        Map<String, Object> rootAttrs = provider.readAttributes(fs.getPath("/"), "basic:isDirectory");
        assertTrue((Boolean) rootAttrs.get("isDirectory"));
      }
    } finally {
      Files.deleteIfExists(gzPath);
    }
  }
}
