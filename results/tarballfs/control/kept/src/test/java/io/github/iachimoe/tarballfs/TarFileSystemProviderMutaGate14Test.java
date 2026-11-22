package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate14Test {
  @Test
  void newFileChannel_optionsHandling_readOnlyAndMissingEntry() throws IOException {
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
      try (FileSystem fs = provider.newFileSystem(tarPath, Map.of())) {
        Path p = fs.getPath("/hello.txt");

        // WRITE without READ is rejected
        assertThrows(
            UnsupportedOperationException.class,
            () -> provider.newFileChannel(p, Set.of(StandardOpenOption.WRITE)));

        // READ combined with a mutating option is rejected
        assertThrows(
            UnsupportedOperationException.class,
            () -> provider.newFileChannel(
                p, Set.of(StandardOpenOption.READ, StandardOpenOption.WRITE)));
        assertThrows(
            UnsupportedOperationException.class,
            () -> provider.newFileChannel(
                p, Set.of(StandardOpenOption.READ, StandardOpenOption.TRUNCATE_EXISTING)));

        // Missing entry
        assertThrows(
            NoSuchFileException.class,
            () -> provider.newFileChannel(fs.getPath("/missing.txt"), Set.of(StandardOpenOption.READ)));

        // Empty options set is treated as read
        try (FileChannel ch = provider.newFileChannel(p, Set.of())) {
          assertEquals(5L, ch.size());
          ByteBuffer buf = ByteBuffer.allocate(16);
          int n = ch.read(buf);
          assertEquals(5, n);
          buf.flip();
          assertEquals("hello", StandardCharsets.UTF_8.decode(buf).toString());
        }
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
