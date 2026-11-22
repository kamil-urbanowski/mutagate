package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import org.junit.jupiter.api.Test;

class AbstractCompressorArchiveFileSystemMutaGate2Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    TestFileSystem(FileSystemProvider provider, URI uri, Path archive, Path temp) {
      super(provider, uri, archive, "test-type", "entry", temp);
    }

    @Override
    protected void deregister(URI uri) {
      // no-op
    }

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return SingleEntryArchiveFormat.GZIP;
    }
  }

  @Test
  void compressorFileStoreReturnsTheStoreExposedByGetFileStores() throws Exception {
    FileSystemProvider provider = FileSystems.getDefault().provider();
    TestFileSystem fs =
        new TestFileSystem(
            provider,
            URI.create("test:///archive.gz"),
            Path.of("archive.gz"),
            Path.of("archive.tmp"));

    ReadOnlyCompressorFileStore store = fs.compressorFileStore();
    assertNotNull(store);

    FileStore listed = fs.getFileStores().iterator().next();
    assertSame(listed, store);
  }
}
