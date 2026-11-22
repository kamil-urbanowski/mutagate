package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate14Test {
  @Test
  void rootDirectoryListing_returnsTopLevelChildrenOnce() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-test-", ".tar");
    try {
      writeNestedTar(tarPath);

      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tarPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        Set<String> names = new HashSet<>();
        int count = 0;
        try (var ds = Files.newDirectoryStream(fs.getPath("/"))) {
          for (Path p : ds) {
            names.add(p.getFileName().toString());
            count++;
          }
        }
        assertEquals(Set.of("root.txt", "dir"), names);
        assertEquals(2, count);
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }

  private static void writeNestedTar(Path tarPath) throws IOException {
    byte[] rootData = "root".getBytes(StandardCharsets.UTF_8);
    byte[] innerData = "inner".getBytes(StandardCharsets.UTF_8);
    byte[] leafData = "leaf".getBytes(StandardCharsets.UTF_8);

    try (OutputStream out = Files.newOutputStream(tarPath);
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
      TarArchiveEntry root = new TarArchiveEntry("root.txt");
      root.setSize(rootData.length);
      tarOut.putArchiveEntry(root);
      tarOut.write(rootData);
      tarOut.closeArchiveEntry();

      TarArchiveEntry inner = new TarArchiveEntry("dir/inner.txt");
      inner.setSize(innerData.length);
      tarOut.putArchiveEntry(inner);
      tarOut.write(innerData);
      tarOut.closeArchiveEntry();

      TarArchiveEntry leaf = new TarArchiveEntry("dir/sub/leaf.txt");
      leaf.setSize(leafData.length);
      tarOut.putArchiveEntry(leaf);
      tarOut.write(leafData);
      tarOut.closeArchiveEntry();
    }
  }
}
