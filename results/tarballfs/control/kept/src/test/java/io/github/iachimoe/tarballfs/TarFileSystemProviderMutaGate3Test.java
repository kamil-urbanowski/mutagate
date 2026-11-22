package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate3Test {
  @Test
  void newByteChannel_optionHandling_sizeAndSeek() throws IOException {
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

        // null options and empty options are treated as read
        try (SeekableByteChannel ch = provider.newByteChannel(p, null)) {
          assertEquals(5L, ch.size());
          assertEquals(0L, ch.position());
        }
        try (SeekableByteChannel ch = provider.newByteChannel(p, Set.<OpenOption>of())) {
          assertEquals(5L, ch.size());
        }

        // READ: read, check position, then seek and re-read
        try (SeekableByteChannel ch = provider.newByteChannel(p, Set.of(StandardOpenOption.READ))) {
          assertTrue(ch.isOpen());
          ByteBuffer buf = ByteBuffer.allocate(16);
          int n = ch.read(buf);
          assertEquals(5, n);
          assertEquals(5L, ch.position());
          buf.flip();
          assertEquals("hello", StandardCharsets.UTF_8.decode(buf).toString());

          ch.position(1);
          ByteBuffer buf2 = ByteBuffer.allocate(16);
          assertEquals(4, ch.read(buf2));
          buf2.flip();
          assertEquals("ello", StandardCharsets.UTF_8.decode(buf2).toString());
        }

        // write-ish options are rejected, even combined with READ
        assertThrows(UnsupportedOperationException.class,
            () -> provider.newByteChannel(p, Set.of(StandardOpenOption.WRITE)));
        assertThrows(UnsupportedOperationException.class,
            () -> provider.newByteChannel(p, Set.of(StandardOpenOption.READ, StandardOpenOption.WRITE)));
        assertThrows(UnsupportedOperationException.class,
            () -> provider.newByteChannel(p, Set.of(StandardOpenOption.READ, StandardOpenOption.APPEND)));
        assertThrows(UnsupportedOperationException.class,
            () -> provider.newByteChannel(p,
                Set.of(StandardOpenOption.READ, StandardOpenOption.TRUNCATE_EXISTING)));
        assertThrows(UnsupportedOperationException.class,
            () -> provider.newByteChannel(p, Set.of(StandardOpenOption.READ, StandardOpenOption.CREATE)));
        assertThrows(UnsupportedOperationException.class,
            () -> provider.newByteChannel(p, Set.of(StandardOpenOption.READ, StandardOpenOption.CREATE_NEW)));

        // non-read option without READ is rejected
        assertThrows(UnsupportedOperationException.class,
            () -> provider.newByteChannel(p, Set.of(StandardOpenOption.SYNC)));

        // missing entry
        Path missing = fs.getPath("/missing.txt");
        assertThrows(NoSuchFileException.class,
            () -> provider.newByteChannel(missing, Set.of(StandardOpenOption.READ)));

        // foreign path type
        Path foreign = Path.of("hello.txt");
        assertThrows(UnsupportedOperationException.class,
            () -> provider.newByteChannel(foreign, Set.of(StandardOpenOption.READ)));
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
