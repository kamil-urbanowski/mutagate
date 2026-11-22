package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

class AbstractCompressorArchiveFileSystemMutaGate3Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    TestFileSystem(URI uri, Path archive, Path tempFile) {
      super(FileSystems.getDefault().provider(), uri, archive, "gzip", "entry", tempFile);
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
  void getFileStoresWorksWhenOpenAndThrowsWhenClosed() throws Exception {
    Path tempFile = Files.createTempFile("mutagate3", ".tmp");
    TestFileSystem fs = new TestFileSystem(
        URI.create("test:///mutagate3"), Path.of("archive.gz"), tempFile);

    Iterable<FileStore> stores = fs.getFileStores();
    Iterator<FileStore> it = stores.iterator();
    assertTrue(it.hasNext());
    assertSame(fs.compressorFileStore(), it.next());

    fs.close();

    assertThrows(ClosedFileSystemException.class, fs::getFileStores);
  }
}
