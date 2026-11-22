package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemMutaGate3Test {
  @Test
  void getPathMatcher_regexNullInvalidAndClosedBehavior() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-test-", ".tar");
    try {
      byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(tarPath);
          TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
        TarArchiveEntry entry = new TarArchiveEntry("hello.txt");
        entry.setSize(data.length);
        tarOut.putArchiveEntry(entry);
        tarOut.write(data);
        tarOut.closeArchiveEntry();
      }

      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tarPath.toAbsolutePath());
      FileSystem fs = provider.newFileSystem(uri, Map.of());
      try {
        PathMatcher regex = fs.getPathMatcher("regex:/.*\\.txt");
        assertNotNull(regex);
        assertTrue(regex.matches(fs.getPath("/hello.txt")));
        assertFalse(regex.matches(fs.getPath("/hello.md")));

        assertThrows(NullPointerException.class, () -> fs.getPathMatcher(null));
        assertThrows(IllegalArgumentException.class, () -> fs.getPathMatcher("noseparator"));
        assertThrows(UnsupportedOperationException.class, () -> fs.getPathMatcher("bogus:*.txt"));
      } finally {
        fs.close();
      }

      assertThrows(ClosedFileSystemException.class, () -> fs.getPathMatcher("glob:*.txt"));
      // The open check happens before the null check.
      assertThrows(ClosedFileSystemException.class, () -> fs.getPathMatcher(null));
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
