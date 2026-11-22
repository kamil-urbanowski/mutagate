package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.URI;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AbstractCompressorArchiveFileSystemMutaGate4Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    TestFileSystem(FileSystemProvider provider, URI uri, Path host, Path temp) {
      super(provider, uri, host, "gzip", "entry", temp);
    }

    @Override
    protected void deregister(URI uri) {
      // nothing to deregister
    }

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return SingleEntryArchiveFormat.GZIP;
    }
  }

  @Test
  void getFileStoresReturnsTheSingleCompressorFileStore() throws Exception {
    Path host = Files.createTempFile("archive", ".gz");
    Path temp = Files.createTempFile("uncompressed", ".tmp");
    try {
      TestFileSystem fs = new TestFileSystem(
          FileSystems.getDefault().provider(), URI.create("test:///archive"), host, temp);
      try {
        List<FileStore> stores = new ArrayList<>();
        for (FileStore store : fs.getFileStores()) {
          stores.add(store);
        }
        assertEquals(1, stores.size());
        assertSame(fs.compressorFileStore(), stores.get(0));
      } finally {
        fs.close();
      }
    } finally {
      Files.deleteIfExists(host);
      Files.deleteIfExists(temp);
    }
  }
}
