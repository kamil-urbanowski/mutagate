package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import org.junit.jupiter.api.Test;

class AbstractCompressorArchiveFileSystemMutaGate11Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    boolean deregistered;

    TestFileSystem(FileSystemProvider provider, URI uri, Path archive, Path temp) {
      super(provider, uri, archive, "gzip", "entry", temp);
    }

    @Override
    protected void deregister(URI uri) {
      deregistered = true;
    }

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return SingleEntryArchiveFormat.GZIP;
    }
  }

  @Test
  void isOpenReflectsClosedState() throws IOException {
    Path archive = Files.createTempFile("mutagate-archive", ".gz");
    Path temp = Files.createTempFile("mutagate-temp", ".tmp");
    try {
      TestFileSystem fs = new TestFileSystem(
          FileSystems.getDefault().provider(), URI.create("test:///mutagate"), archive, temp);
      assertTrue(fs.isOpen());
      fs.close();
      assertFalse(fs.isOpen());
      assertTrue(fs.deregistered);
    } finally {
      Files.deleteIfExists(archive);
      Files.deleteIfExists(temp);
    }
  }
}
