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

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemMutaGate13Test {
  @Test
  void newWatchService_throwsUnsupportedOperationWithMessage_onOpenAndClosedFileSystem() throws IOException {
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

      UnsupportedOperationException openEx =
          assertThrows(UnsupportedOperationException.class, fs::newWatchService);
      assertEquals("Watch service is not implemented yet", openEx.getMessage());
      assertTrue(fs.isOpen());

      fs.close();
      assertFalse(fs.isOpen());

      UnsupportedOperationException closedEx =
          assertThrows(UnsupportedOperationException.class, fs::newWatchService);
      assertEquals("Watch service is not implemented yet", closedEx.getMessage());
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
