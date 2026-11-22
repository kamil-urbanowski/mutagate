package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemMutaGate11Test {
  @Test
  void supportedFileAttributeViews_onlyBasic_andUnmodifiable() throws IOException {
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
      Set<String> views = fs.supportedFileAttributeViews();

      assertEquals(1, views.size());
      assertEquals(Set.of("basic"), views);
      assertTrue(views.contains("basic"));
      assertFalse(views.contains("posix"));
      assertFalse(views.contains("unix"));
      assertThrows(UnsupportedOperationException.class, () -> views.add("posix"));

      fs.close();
      // Still available after close, since the method does not check open state.
      assertEquals(Set.of("basic"), fs.supportedFileAttributeViews());
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
