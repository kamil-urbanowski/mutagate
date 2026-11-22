package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.Test;

final class Bzip2FileSystemProviderMutaGate5Test {
  @Test
  void newFileSystemPath_nullRejected_duplicateRejectedWhileOpen_reopenAllowedAfterClose() throws IOException {
    Bzip2FileSystemProvider provider = new Bzip2FileSystemProvider();

    assertThrows(NullPointerException.class, () -> provider.newFileSystem((Path) null, Map.of()));

    Path bzPath = Files.createTempFile("tarballfs-muta-", ".bz2");
    try {
      byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(bzPath);
          BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(out)) {
        bzOut.write(data);
      }

      FileSystem first = provider.newFileSystem(bzPath, Map.of());
      try {
        assertNotNull(first);
        assertTrue(first.isOpen());
        assertThrows(FileSystemAlreadyExistsException.class, () -> provider.newFileSystem(bzPath, Map.of()));
        assertTrue(first.isOpen());
      } finally {
        first.close();
      }

      try (FileSystem second = provider.newFileSystem(bzPath, Map.of())) {
        assertNotSame(first, second);
        assertTrue(second.isOpen());
        assertEquals("hello", Files.readString(second.getPath("/", bzPath.getFileName().toString())));
      }
    } finally {
      Files.deleteIfExists(bzPath);
    }
  }
}
