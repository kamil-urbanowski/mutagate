package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemMutaGate2Test {
  @Test
  void close_isIdempotent_deregistersUri_andDoesNotAffectReplacementFileSystem() throws IOException {
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

      FileSystem fs1 = provider.newFileSystem(uri, Map.of());
      assertTrue(fs1.isOpen());

      fs1.close();
      assertFalse(fs1.isOpen());
      assertThrows(ClosedFileSystemException.class, () -> fs1.getPath("/hello.txt"));
      assertThrows(ClosedFileSystemException.class, fs1::getRootDirectories);
      assertThrows(ClosedFileSystemException.class, fs1::getFileStores);

      // After close the URI must be free again, so a new file system can be created.
      FileSystem fs2 = provider.newFileSystem(uri, Map.of());
      try {
        assertNotSame(fs1, fs2);
        assertTrue(fs2.isOpen());

        // Closing the old file system again must be a no-op and must not deregister fs2.
        fs1.close();
        assertFalse(fs1.isOpen());
        assertTrue(fs2.isOpen());
        assertEquals("hello", Files.readString(fs2.getPath("/hello.txt"), StandardCharsets.UTF_8));
      } finally {
        fs2.close();
      }
      assertFalse(fs2.isOpen());
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
