package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate13Test {
  @Test
  void newByteChannel_readWithWriteOption_isRejectedAsReadOnly() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-mutagate-", ".tar");
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

        // READ together with WRITE must be rejected by the loop over options.
        UnsupportedOperationException ex = assertThrows(
            UnsupportedOperationException.class,
            () -> provider.newByteChannel(p, Set.of(StandardOpenOption.READ, StandardOpenOption.WRITE)));
        assertEquals("Tar is read-only", ex.getMessage());

        // Plain READ still works.
        try (SeekableByteChannel ch = provider.newByteChannel(p, Set.of(StandardOpenOption.READ))) {
          assertNotNull(ch);
          assertEquals(data.length, ch.size());
        }
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
