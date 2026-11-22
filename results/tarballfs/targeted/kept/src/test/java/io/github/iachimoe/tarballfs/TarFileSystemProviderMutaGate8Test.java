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

final class TarFileSystemProviderMutaGate8Test {
  @Test
  void getFileSystem_returnsRegisteredFileSystem_andThrowsForUnregistered() throws IOException {
    Path registeredTar = Files.createTempFile("tarballfs-mg8-a-", ".tar");
    Path otherTar = Files.createTempFile("tarballfs-mg8-b-", ".tar");
    try {
      writeSimpleTar(registeredTar);
      writeSimpleTar(otherTar);

      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI registeredUri = URI.create("tar:" + registeredTar.toAbsolutePath());
      URI otherUri = URI.create("tar:" + otherTar.toAbsolutePath());

      try (FileSystem fs = provider.newFileSystem(registeredUri, Map.of())) {
        assertSame(fs, provider.getFileSystem(registeredUri));
        assertThrows(FileSystemNotFoundException.class, () -> provider.getFileSystem(otherUri));
      }
    } finally {
      Files.deleteIfExists(registeredTar);
      Files.deleteIfExists(otherTar);
    }
  }

  private static void writeSimpleTar(Path tarPath) throws IOException {
    byte[] data = "hello".getBytes(StandardCharsets.UTF_8);

    try (OutputStream out = Files.newOutputStream(tarPath);
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
      TarArchiveEntry entry = new TarArchiveEntry("hello.txt");
      entry.setSize(data.length);
      tarOut.putArchiveEntry(entry);
      tarOut.write(data);
      tarOut.closeArchiveEntry();
    }
  }
}
