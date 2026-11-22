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

final class TarFileSystemProviderMutaGate19Test {
  @Test
  void newDirectoryStream_withFilter_returnsOnlyAcceptedEntriesWithoutDuplicates() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-test-", ".tar");
    try {
      writeTar(tarPath);

      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tarPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        DirectoryStream.Filter<Path> filter = p -> p.getFileName().toString().equals("root.txt");
        List<String> names = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(fs.getPath("/"), filter)) {
          for (Path p : ds) {
            names.add(p.getFileName().toString());
          }
        }
        assertEquals(List.of("root.txt"), names);
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }

  private static void writeTar(Path tarPath) throws IOException {
    byte[] rootData = "root".getBytes(StandardCharsets.UTF_8);
    byte[] innerData = "inner".getBytes(StandardCharsets.UTF_8);

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
    }
  }
}
