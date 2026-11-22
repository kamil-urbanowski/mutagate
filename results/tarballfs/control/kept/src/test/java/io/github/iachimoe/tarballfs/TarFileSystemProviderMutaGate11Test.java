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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate11Test {
  @Test
  void newFileSystem_uri_rejectsNullAndWrongScheme_andDuplicateOpenFileSystem() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-gate11-", ".tar");
    try {
      byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(tarPath);
          TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
        TarArchiveEntry entry = new TarArchiveEntry("hello.txt");
        entry.setSize(data.length);
        tarOut.putArchiveEntry(entry);
        tarOut.write(data);
        tarOut.closeArchiveEntry();
      }

      TarFileSystemProvider provider = new TarFileSystemProvider();

      assertThrows(NullPointerException.class, () -> provider.newFileSystem((URI) null, Map.of()));

      URI wrongScheme = URI.create("file://" + tarPath.toAbsolutePath());
      IllegalArgumentException iae =
          assertThrows(IllegalArgumentException.class, () -> provider.newFileSystem(wrongScheme, Map.of()));
      assertTrue(iae.getMessage().contains("file"));

      URI uri = URI.create("tar:" + tarPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        assertTrue(fs.isOpen());
        assertThrows(FileSystemAlreadyExistsException.class, () -> provider.newFileSystem(uri, Map.of()));
        assertThrows(
            FileSystemAlreadyExistsException.class, () -> provider.newFileSystem(tarPath, Map.of()));
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
