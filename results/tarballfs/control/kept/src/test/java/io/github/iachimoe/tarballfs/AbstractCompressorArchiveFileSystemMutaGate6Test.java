package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AbstractCompressorArchiveFileSystemMutaGate6Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    int deregisterCalls;

    TestFileSystem(FileSystemProvider provider, URI uri, Path archive, Path temp) {
      super(provider, uri, archive, "gzip", "entry", temp);
    }

    @Override
    protected void deregister(URI uri) {
      deregisterCalls++;
    }

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return SingleEntryArchiveFormat.GZIP;
    }
  }

  @TempDir
  Path tempDir;

  @Test
  void getFileStoresReturnsSingleReadOnlyStoreAndFailsAfterClose() throws IOException {
    Path archive = tempDir.resolve("a.gz");
    Files.write(archive, new byte[] {1, 2, 3});
    Path temp = tempDir.resolve("a.tmp");
    Files.write(temp, new byte[] {4});

    TestFileSystem fs =
        new TestFileSystem(
            FileSystems.getDefault().provider(), URI.create("tarballfs:gz:test"), archive, temp);

    Iterable<FileStore> stores = fs.getFileStores();
    assertNotNull(stores);

    List<FileStore> collected = new ArrayList<>();
    for (FileStore store : stores) {
      collected.add(store);
    }
    assertEquals(1, collected.size());
    assertSame(fs.compressorFileStore(), collected.get(0));
    assertTrue(collected.get(0).isReadOnly());

    // repeated calls return the same store
    Iterator<FileStore> second = fs.getFileStores().iterator();
    assertSame(fs.compressorFileStore(), second.next());
    assertTrue(!second.hasNext());

    // the returned collection is unmodifiable
    Iterator<FileStore> it = fs.getFileStores().iterator();
    it.next();
    assertThrows(UnsupportedOperationException.class, it::remove);

    fs.close();
    assertEquals(1, fs.deregisterCalls);
    assertThrows(ClosedFileSystemException.class, fs::getFileStores);
  }
}
