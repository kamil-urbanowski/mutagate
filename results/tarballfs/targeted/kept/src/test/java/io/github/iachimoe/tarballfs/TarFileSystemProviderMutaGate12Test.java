package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate12Test {
  @Test
  void newByteChannel_readFlagLogic_acceptsNullEmptyAndReadButRejectsOthers() throws IOException {
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

        // null options: allowed
        Set<OpenOption> nullOptions = null;
        try (SeekableByteChannel ch = provider.newByteChannel(p, nullOptions)) {
          assertEquals(5L, ch.size());
        }

        // empty options: allowed
        Set<OpenOption> empty = Set.of();
        try (SeekableByteChannel ch = provider.newByteChannel(p, empty)) {
          ByteBuffer buf = ByteBuffer.allocate(16);
          ch.read(buf);
          buf.flip();
          assertEquals("hello", StandardCharsets.UTF_8.decode(buf).toString());
        }

        // READ option: allowed
        Set<OpenOption> readOnly = Set.of(StandardOpenOption.READ);
        try (SeekableByteChannel ch = provider.newByteChannel(p, readOnly)) {
          assertEquals(5L, ch.size());
        }

        // non-empty options without READ (and not a write option): rejected
        Set<OpenOption> noRead = Set.of(StandardOpenOption.SPARSE);
        assertThrows(UnsupportedOperationException.class, () -> provider.newByteChannel(p, noRead));
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
