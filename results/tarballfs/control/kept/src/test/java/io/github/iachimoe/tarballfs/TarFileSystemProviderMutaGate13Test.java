package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate13Test {
  @Test
  void newFileSystem_path_nullRejected_andDuplicateOpenRejected_perProvider() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-test-", ".tar");
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

      assertThrows(NullPointerException.class, () -> provider.newFileSystem((Path) null, Map.of()));

      try (FileSystem first = provider.newFileSystem(tarPath, Map.of())) {
        assertNotNull(first);
        assertTrue(first.isOpen());

        // Same archive on the same provider while still open must be rejected.
        assertThrows(FileSystemAlreadyExistsException.class, () -> provider.newFileSystem(tarPath, Map.of()));
        assertTrue(first.isOpen());

        // A different provider keeps its own registry, so it may open the same archive.
        TarFileSystemProvider other = new TarFileSystemProvider();
        try (FileSystem second = other.newFileSystem(tarPath, Map.of())) {
          assertNotNull(second);
          assertNotSame(first, second);
        }
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
