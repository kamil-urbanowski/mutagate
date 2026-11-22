package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate4Test {
  @Test
  void directoryListing_skipsEntriesWithEmptySegmentAfterPrefix() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-test-", ".tar");
    try {
      byte[] data = "x".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(tarPath);
          TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
        for (String name : new String[] {"dir//odd", "dir/real.txt"}) {
          TarArchiveEntry entry = new TarArchiveEntry(name);
          entry.setSize(data.length);
          tarOut.putArchiveEntry(entry);
          tarOut.write(data);
          tarOut.closeArchiveEntry();
        }
      }

      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tarPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        List<String> names = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(fs.getPath("/dir"))) {
          for (Path p : ds) {
            names.add(p.toString());
          }
        }
        assertEquals(1, names.size());
        assertEquals("/dir/real.txt", names.get(0));
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
