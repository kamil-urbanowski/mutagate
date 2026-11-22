package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate10Test {
  @Test
  void getFileSystem_returnsRegisteredInstance_andThrowsWhenNotRegistered() throws IOException {
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

      URI uri = URI.create("tar:" + tarPath.toAbsolutePath());
      TarFileSystemProvider provider = new TarFileSystemProvider();

      // Not yet registered.
      assertThrows(FileSystemNotFoundException.class, () -> provider.getFileSystem(uri));

      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        assertSame(fs, provider.getFileSystem(uri));
        // Repeated lookups return the same instance.
        assertSame(fs, provider.getFileSystem(uri));

        // A different provider has its own registry and does not know about this file system.
        TarFileSystemProvider other = new TarFileSystemProvider();
        assertThrows(FileSystemNotFoundException.class, () -> other.getFileSystem(uri));
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
