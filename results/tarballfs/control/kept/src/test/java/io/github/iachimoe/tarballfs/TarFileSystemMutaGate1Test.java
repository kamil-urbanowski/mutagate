package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

final class TarFileSystemMutaGate1Test {
  @Test
  void getPath_joinsSegments_skipsNullAndEmpty_andFailsWhenClosed() throws IOException {
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

      TarPath abs = (TarPath) fs.getPath("/a", "b", "", null, "c");
      assertTrue(abs.isAbsolute());
      assertEquals("a/b/c", abs.entryName());

      TarPath rel = (TarPath) fs.getPath("x", "y");
      assertFalse(rel.isAbsolute());
      assertEquals("x/y", rel.entryName());

      TarPath emptyFirst = (TarPath) fs.getPath("", "b");
      assertFalse(emptyFirst.isAbsolute());
      assertEquals("b", emptyFirst.entryName());

      TarPath nullFirst = (TarPath) fs.getPath(null, "b", "c");
      assertFalse(nullFirst.isAbsolute());
      assertEquals("b/c", nullFirst.entryName());

      TarPath nullMore = (TarPath) fs.getPath("a", (String[]) null);
      assertFalse(nullMore.isAbsolute());
      assertEquals("a", nullMore.entryName());

      TarPath slashThenMore = (TarPath) fs.getPath("/", "q");
      assertTrue(slashThenMore.isAbsolute());
      assertEquals("q", slashThenMore.entryName());

      fs.close();
      assertThrows(ClosedFileSystemException.class, () -> fs.getPath("/a"));
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
