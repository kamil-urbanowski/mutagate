package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate17Test {
  @Test
  void newDirectoryStream_missingDirectory_throwsNoSuchFile_whileExistingAndRootList() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-test-", ".tar");
    try {
      byte[] data = "inner".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(tarPath);
          TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
        TarArchiveEntry entry = new TarArchiveEntry("dir/inner.txt");
        entry.setSize(data.length);
        tarOut.putArchiveEntry(entry);
        tarOut.write(data);
        tarOut.closeArchiveEntry();
      }

      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tarPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        assertThrows(
            NoSuchFileException.class,
            () -> provider.newDirectoryStream(fs.getPath("/missing"), null));

        Set<String> existing = new HashSet<>();
        try (DirectoryStream<Path> ds = provider.newDirectoryStream(fs.getPath("/dir"), null)) {
          for (Path p : ds) {
            existing.add(p.getFileName().toString());
          }
        }
        assertEquals(Set.of("inner.txt"), existing);

        Set<String> root = new HashSet<>();
        try (DirectoryStream<Path> ds = provider.newDirectoryStream(fs.getPath("/"), null)) {
          for (Path p : ds) {
            root.add(p.getFileName().toString());
          }
        }
        assertEquals(Set.of("dir"), root);
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
