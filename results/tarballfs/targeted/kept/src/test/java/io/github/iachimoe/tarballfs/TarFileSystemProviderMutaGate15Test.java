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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate15Test {
  @Test
  void rootDirectoryListing_returnsTopLevelChildrenOnce() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-muta-", ".tar");
    try {
      try (OutputStream out = Files.newOutputStream(tarPath);
          TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
        String[] names = {"root.txt", "dir/inner.txt", "dir/sub/leaf.txt"};
        for (String n : names) {
          byte[] data = n.getBytes(StandardCharsets.UTF_8);
          TarArchiveEntry e = new TarArchiveEntry(n);
          e.setSize(data.length);
          tarOut.putArchiveEntry(e);
          tarOut.write(data);
          tarOut.closeArchiveEntry();
        }
      }

      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tarPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        Set<String> found = new HashSet<>();
        int count = 0;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(fs.getPath("/"))) {
          for (Path p : ds) {
            found.add(p.getFileName().toString());
            count++;
          }
        }
        assertEquals(Set.of("root.txt", "dir"), found);
        assertEquals(2, count);
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
