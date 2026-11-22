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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate1Test {

  @Test
  void newDirectoryStream_rootDedupesFiltersAndRejectsMissingDirectories() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-mutagate-", ".tar");
    try {
      try (OutputStream out = Files.newOutputStream(tarPath);
          TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
        addFile(tarOut, "a.txt", "a");
        addFile(tarOut, "dir/x.txt", "x");
        addFile(tarOut, "dir/sub/y.txt", "y");
        addFile(tarOut, "dir/sub/z.txt", "z");
        addFile(tarOut, "other/q.txt", "q");
      }

      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tarPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        // Root listing: unique first segments, in archive order.
        assertEquals(List.of("a.txt", "dir", "other"), names(fs.getPath("/"), null));

        // Nested directory: "sub" appears once even though two entries live under it.
        assertEquals(List.of("x.txt", "sub"), names(fs.getPath("/dir"), null));

        // Deeper directory lists only its immediate children.
        assertEquals(List.of("y.txt", "z.txt"), names(fs.getPath("/dir/sub"), null));

        // Filter is applied to the children.
        assertEquals(
            List.of("x.txt"),
            names(fs.getPath("/dir"), p -> p.getFileName().toString().endsWith(".txt")));
        assertEquals(List.of(), names(fs.getPath("/dir"), p -> false));

        // Missing directory and a regular file both have no children -> NoSuchFileException.
        assertThrows(NoSuchFileException.class, () -> Files.newDirectoryStream(fs.getPath("/nope")));
        assertThrows(NoSuchFileException.class, () -> Files.newDirectoryStream(fs.getPath("/a.txt")));
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }

  private static List<String> names(Path dir, DirectoryStream.Filter<Path> filter) throws IOException {
    List<String> result = new ArrayList<>();
    try (DirectoryStream<Path> ds =
        filter == null ? Files.newDirectoryStream(dir) : Files.newDirectoryStream(dir, filter)) {
      for (Path p : ds) {
        result.add(p.getFileName().toString());
      }
    }
    return result;
  }

  private static void addFile(TarArchiveOutputStream tarOut, String name, String content) throws IOException {
    byte[] data = content.getBytes(StandardCharsets.UTF_8);
    TarArchiveEntry entry = new TarArchiveEntry(name);
    entry.setSize(data.length);
    tarOut.putArchiveEntry(entry);
    tarOut.write(data);
    tarOut.closeArchiveEntry();
  }
}
