package io.github.iachimoe.tarballfs;

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

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.Test;

final class Bzip2FileSystemProviderMutaGate3Test {
  @Test
  void newFileSystemUri_rejectsNullAndWrongScheme_andDuplicateOpenMount() throws IOException {
    Bzip2FileSystemProvider provider = new Bzip2FileSystemProvider();

    assertThrows(NullPointerException.class, () -> provider.newFileSystem((URI) null, Map.of()));

    URI wrongScheme = URI.create("gzip:/tmp/does-not-matter.gz");
    IllegalArgumentException iae =
        assertThrows(IllegalArgumentException.class, () -> provider.newFileSystem(wrongScheme, Map.of()));
    assertTrue(iae.getMessage().contains("gzip"));

    Path bzPath = Files.createTempFile("tarballfs-test-", ".bz2");
    try {
      try (OutputStream out = Files.newOutputStream(bzPath);
          BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(out)) {
        bzOut.write("hello".getBytes(StandardCharsets.UTF_8));
      }

      URI uri = URI.create("bzip2:" + bzPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        assertTrue(fs.isOpen());
        assertThrows(FileSystemAlreadyExistsException.class, () -> provider.newFileSystem(uri, Map.of()));
        assertThrows(FileSystemAlreadyExistsException.class, () -> provider.newFileSystem(bzPath, Map.of()));
        assertTrue(fs.isOpen());
      }
    } finally {
      Files.deleteIfExists(bzPath);
    }
  }
}
