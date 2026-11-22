package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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

final class TarFileSystemProviderMutaGate1Test {
  @Test
  void newFileSystem_whileFirstStillOpen_throwsFileSystemAlreadyExistsException() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-mutagate-", ".tar");
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
      try (FileSystem first = provider.newFileSystem(tarPath, Map.of())) {
        assertNotNull(first);
        assertTrue(first.isOpen());

        assertThrows(
            FileSystemAlreadyExistsException.class,
            () -> provider.newFileSystem(tarPath, Map.of()));
        assertThrows(
            FileSystemAlreadyExistsException.class,
            () -> provider.newFileSystem(URI.create("tar:" + tarPath.toAbsolutePath()), Map.of()));
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
