package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.Test;

final class Bzip2FileSystemProviderMutaGate4Test {
  @Test
  void readAttributes_stringForm_returnsRequestedBasicAttributes() throws IOException {
    Path bzPath = Files.createTempFile("tarballfs-test-", ".bz2");
    try {
      byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(bzPath);
          BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(out)) {
        bzOut.write(data);
      }

      Bzip2FileSystemProvider provider = new Bzip2FileSystemProvider();
      URI uri = URI.create("bzip2:" + bzPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        Path file = fs.getPath("/", bzPath.getFileName().toString());
        Map<String, Object> fileAttrs =
            provider.readAttributes(file, "basic:size,isRegularFile,isDirectory");
        assertEquals(5L, fileAttrs.get("size"));
        assertEquals(Boolean.TRUE, fileAttrs.get("isRegularFile"));
        assertEquals(Boolean.FALSE, fileAttrs.get("isDirectory"));

        Map<String, Object> rootAttrs =
            Files.readAttributes(fs.getPath("/"), "basic:isDirectory,isRegularFile");
        assertEquals(Boolean.TRUE, rootAttrs.get("isDirectory"));
        assertEquals(Boolean.FALSE, rootAttrs.get("isRegularFile"));
      }
    } finally {
      Files.deleteIfExists(bzPath);
    }
  }
}
