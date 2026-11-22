package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate18Test {
  @Test
  void newDirectoryStream_appliesFilterWhenPresent_andAllowsNullFilter() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-mutagate-", ".tar");
    try {
      byte[] innerData = "inner".getBytes(StandardCharsets.UTF_8);
      byte[] leafData = "leaf".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(tarPath);
          TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
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

      TarFileSystemProvider provider = new TarFileSystemProvider();
      try (FileSystem fs = provider.newFileSystem(tarPath, Map.of())) {
        Path dir = fs.getPath("/dir");

        DirectoryStream.Filter<Path> onlyTxt = p -> p.getFileName().toString().endsWith(".txt");
        Set<String> filtered = new TreeSet<>();
        try (DirectoryStream<Path> ds = provider.newDirectoryStream(dir, onlyTxt)) {
          for (Path p : ds) {
            filtered.add(p.getFileName().toString());
          }
        }
        assertEquals(Set.of("inner.txt"), filtered);

        Set<String> unfiltered = new TreeSet<>();
        try (DirectoryStream<Path> ds = provider.newDirectoryStream(dir, null)) {
          for (Path p : ds) {
            unfiltered.add(p.getFileName().toString());
          }
        }
        assertEquals(Set.of("inner.txt", "sub"), unfiltered);
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
