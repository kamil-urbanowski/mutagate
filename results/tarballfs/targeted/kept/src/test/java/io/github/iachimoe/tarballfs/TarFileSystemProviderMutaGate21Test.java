package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate21Test {
  @Test
  void directoryEntryWithTrailingSlash_isListedWithoutTrailingSlash() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-muta-", ".tar");
    try {
      byte[] data = "x".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(tarPath);
          TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
        TarArchiveEntry dirEntry = new TarArchiveEntry("dir/");
        tarOut.putArchiveEntry(dirEntry);
        tarOut.closeArchiveEntry();

        TarArchiveEntry fileEntry = new TarArchiveEntry("dir/file.txt");
        fileEntry.setSize(data.length);
        tarOut.putArchiveEntry(fileEntry);
        tarOut.write(data);
        tarOut.closeArchiveEntry();
      }

      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tarPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        Set<String> names = new TreeSet<>();
        try (var ds = Files.newDirectoryStream(fs.getPath("/"))) {
          for (Path p : ds) {
            names.add(p.getFileName().toString());
          }
        }
        assertEquals(Set.of("dir"), names);
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
