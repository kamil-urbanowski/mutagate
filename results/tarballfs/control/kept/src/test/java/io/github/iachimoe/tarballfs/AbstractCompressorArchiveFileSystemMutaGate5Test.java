package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AbstractCompressorArchiveFileSystemMutaGate5Test {

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
  void rootDirectoriesContainsSingleAbsoluteRootAndFailsAfterClose() throws IOException {
    Path host = Files.createTempFile("mutagate-host", ".gz");
    Path temp = Files.createTempFile("mutagate-temp", ".tmp");
    try {
      TestFileSystem fs =
          new TestFileSystem(
              FileSystems.getDefault().provider(), URI.create("test:///mutagate5"), host, temp);

      Iterable<Path> roots = fs.getRootDirectories();
      List<Path> first = new ArrayList<>();
      roots.forEach(first::add);

      assertEquals(1, first.size());
      Path root = first.get(0);
      assertTrue(root instanceof SingleEntryArchivePath);
      assertTrue(root.isAbsolute());
      assertSame(fs, root.getFileSystem());
      assertEquals(SingleEntryArchiveFormat.GZIP, ((SingleEntryArchivePath) root).format());

      List<Path> second = new ArrayList<>();
      fs.getRootDirectories().forEach(second::add);
      assertEquals(first, second);

      fs.close();
      assertFalse(fs.isOpen());
      assertThrows(ClosedFileSystemException.class, fs::getRootDirectories);
    } finally {
      Files.deleteIfExists(host);
      Files.deleteIfExists(temp);
    }
  }
}
