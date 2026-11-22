package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessMode;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate22Test {
  @Test
  void trailingSlashOfDirectoryEntryIsStrippedWhenNormalizing() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-mutagate-", ".tar");
    try {
      byte[] data = "abc".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(tarPath);
          TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
        TarArchiveEntry dirEntry = new TarArchiveEntry("emptydir/");
        tarOut.putArchiveEntry(dirEntry);
        tarOut.closeArchiveEntry();

        TarArchiveEntry file = new TarArchiveEntry("a.txt");
        file.setSize(data.length);
        tarOut.putArchiveEntry(file);
        tarOut.write(data);
        tarOut.closeArchiveEntry();
      }

      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tarPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        Path file = fs.getPath("/a.txt");
        assertDoesNotThrow(() -> provider.checkAccess(file, AccessMode.READ));

        Path emptyDir = fs.getPath("/emptydir");
        assertThrows(NoSuchFileException.class, () -> provider.checkAccess(emptyDir, AccessMode.READ));
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
