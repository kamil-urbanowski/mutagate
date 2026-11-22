package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate5Test {
  @Test
  void getFileAttributeView_tarPathReturnsBasicView_nonTarPathThrows() throws IOException {
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
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        Path p = fs.getPath("/hello.txt");

        BasicFileAttributeView view = provider.getFileAttributeView(p, BasicFileAttributeView.class);
        assertNotNull(view);
        assertEquals("basic", view.name());
        BasicFileAttributes attrs = view.readAttributes();
        assertTrue(attrs.isRegularFile());
        assertEquals(data.length, attrs.size());

        assertNull(provider.getFileAttributeView(p, FileOwnerAttributeView.class));

        Path foreign = tarPath.toAbsolutePath();
        assertThrows(
            UnsupportedOperationException.class,
            () -> provider.getFileAttributeView(foreign, BasicFileAttributeView.class));
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
