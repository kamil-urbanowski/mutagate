package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

final class GzipFileSystemMutaGate1Test {
  @Test
  void constructor_storesArgumentsAndConfiguresGzipFileSystem() {
    GzipFileSystemProvider provider = new GzipFileSystemProvider();
    Path gzipPath = Path.of("some-dir", "archive.gz");
    Path tempFile = Path.of("some-dir", "payload.tmp");
    URI uri = URI.create("gzip:archive.gz");

    GzipFileSystem fs = new GzipFileSystem(provider, uri, gzipPath, "entry.txt", tempFile);

    assertSame(provider, fs.provider());
    assertSame(gzipPath, fs.gzipFile());
    assertSame(gzipPath, fs.hostArchivePath());
    assertEquals("entry.txt", fs.virtualEntryName());
    assertSame(tempFile, fs.uncompressedTempFile());
    assertEquals("gzip:" + gzipPath, fs.toString());
    assertEquals(SingleEntryArchiveFormat.GZIP, fs.pathFormat());

    ReadOnlyCompressorFileStore store = fs.gzipFileStore();
    assertNotNull(store);
    assertSame(store, fs.compressorFileStore());
    assertEquals("gzip", store.type());

    assertTrue(fs.isOpen());
    assertTrue(fs.isReadOnly());
  }
}
