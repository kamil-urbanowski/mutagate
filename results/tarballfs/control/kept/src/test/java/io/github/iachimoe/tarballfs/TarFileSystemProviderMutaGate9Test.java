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
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate9Test {
  @Test
  void getFileAttributeView_basicViewSupported_otherViewsNull_foreignPathRejected() throws IOException {
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

        // Each call yields a fresh view instance.
        BasicFileAttributeView another = provider.getFileAttributeView(p, BasicFileAttributeView.class);
        assertNotNull(another);

        // Unsupported view types yield null rather than throwing.
        assertNull(provider.getFileAttributeView(p, PosixFileAttributeView.class));
        assertNull(provider.getFileAttributeView(p, FileOwnerAttributeView.class));
        assertNull(provider.getFileAttributeView(p, FileAttributeView.class));

        // A path from another file system is rejected.
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
