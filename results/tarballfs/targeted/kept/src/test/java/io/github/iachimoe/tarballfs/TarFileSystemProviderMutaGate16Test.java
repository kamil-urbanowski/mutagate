package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate16Test {
  @Test
  void rootDirectoryListing_returnsEachTopLevelChildExactlyOnce() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-mutagate16-", ".tar");
    try {
      writeTar(tarPath);

      TarFileSystemProvider provider = new TarFileSystemProvider();
      try (FileSystem fs = provider.newFileSystem(tarPath, Map.of())) {
        List<String> names = new ArrayList<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(fs.getPath("/"))) {
          for (Path p : ds) {
            names.add(p.getFileName().toString());
          }
        }
        Collections.sort(names);
        assertEquals(List.of("dir", "root.txt"), names);
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }

  private static void writeTar(Path tarPath) throws IOException {
    try (OutputStream out = Files.newOutputStream(tarPath);
        TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
      addEntry(tarOut, "root.txt", "root");
      addEntry(tarOut, "dir/inner.txt", "inner");
      addEntry(tarOut, "dir/sub/leaf.txt", "leaf");
    }
  }

  private static void addEntry(TarArchiveOutputStream tarOut, String name, String content)
      throws IOException {
    byte[] data = content.getBytes(StandardCharsets.UTF_8);
    TarArchiveEntry entry = new TarArchiveEntry(name);
    entry.setSize(data.length);
    tarOut.putArchiveEntry(entry);
    tarOut.write(data);
    tarOut.closeArchiveEntry();
  }
}
