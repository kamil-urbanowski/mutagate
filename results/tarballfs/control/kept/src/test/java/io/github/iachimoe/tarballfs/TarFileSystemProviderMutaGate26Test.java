package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate26Test {
  @Test
  void deregister_removesRegistration_allowsNewFileSystemWhileOldStillOpen() throws IOException {
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
      URI key = URI.create("tar:" + tarPath.toAbsolutePath());

      FileSystem first = provider.newFileSystem(tarPath, Map.of());
      FileSystem second = null;
      try {
        // Registered while open.
        assertNotNull(provider.getPath(key));
        assertThrows(FileSystemAlreadyExistsException.class, () -> provider.newFileSystem(tarPath, Map.of()));

        // Deregistering an unrelated URI does not affect the registration.
        assertDoesNotThrow(() -> provider.deregister(URI.create("tar:/no/such/archive.tar")));
        assertNotNull(provider.getPath(key));

        provider.deregister(key);

        // Registration is gone even though the file system is still open.
        assertTrue(first.isOpen());
        assertThrows(FileSystemNotFoundException.class, () -> provider.getPath(key));

        // A new file system can now be created for the same archive.
        second = provider.newFileSystem(tarPath, Map.of());
        assertNotSame(first, second);
        assertNotNull(provider.getPath(key));
      } finally {
        first.close();
        if (second != null) {
          second.close();
        }
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
