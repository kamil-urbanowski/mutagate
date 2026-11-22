package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import org.junit.jupiter.api.Test;

class AbstractCompressorArchiveFileSystemMutaGate1Test {

  private static final class TestFs extends AbstractCompressorArchiveFileSystem {
    TestFs(
        FileSystemProvider provider,
        URI uri,
        Path hostArchivePath,
        String fileStoreType,
        String virtualEntryName,
        Path uncompressedTempFile) {
      super(provider, uri, hostArchivePath, fileStoreType, virtualEntryName, uncompressedTempFile);
    }

    @Override
    protected void deregister(URI uri) {
    }

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return SingleEntryArchiveFormat.GZIP;
    }
  }

  @Test
  void constructorStoresArgumentsAndRejectsNulls() {
    FileSystemProvider provider = FileSystems.getDefault().provider();
    URI uri = URI.create("gzip:file:///tmp/a.gz");
    Path archive = Path.of("a.gz");
    Path temp = Path.of("a.tmp");

    TestFs fs = new TestFs(provider, uri, archive, "gzip", "a", temp);
    assertSame(provider, fs.provider());
    assertSame(archive, fs.hostArchivePath());
    assertEquals("a", fs.virtualEntryName());
    assertSame(temp, fs.uncompressedTempFile());
    assertNotNull(fs.compressorFileStore());
    assertTrue(fs.isOpen());
    assertTrue(fs.isReadOnly());

    NullPointerException e =
        assertThrows(NullPointerException.class, () -> new TestFs(null, uri, archive, "gzip", "a", temp));
    assertEquals("provider", e.getMessage());

    e = assertThrows(NullPointerException.class, () -> new TestFs(provider, null, archive, "gzip", "a", temp));
    assertEquals("uri", e.getMessage());

    e = assertThrows(NullPointerException.class, () -> new TestFs(provider, uri, null, "gzip", "a", temp));
    assertEquals("hostArchivePath", e.getMessage());

    e = assertThrows(NullPointerException.class, () -> new TestFs(provider, uri, archive, null, "a", temp));
    assertNull(e.getMessage());

    e = assertThrows(NullPointerException.class, () -> new TestFs(provider, uri, archive, "gzip", null, temp));
    assertEquals("virtualEntryName", e.getMessage());

    e = assertThrows(NullPointerException.class, () -> new TestFs(provider, uri, archive, "gzip", "a", null));
    assertEquals("uncompressedTempFile", e.getMessage());
  }
}
