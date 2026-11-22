package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate9Test {
  @Test
  void getFileSystem_returnsTheRegisteredFileSystem() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-mutagate9-", ".tar");
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
      URI uri = URI.create("tar:" + tarPath.toAbsolutePath());
      try (FileSystem created = provider.newFileSystem(uri, Map.of())) {
        FileSystem found = provider.getFileSystem(uri);
        assertNotNull(found);
        assertSame(created, found);
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
