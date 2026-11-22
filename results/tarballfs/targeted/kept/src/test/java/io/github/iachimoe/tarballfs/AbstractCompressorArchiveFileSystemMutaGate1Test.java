package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AbstractCompressorArchiveFileSystemMutaGate1Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    final List<URI> deregistered = new ArrayList<>();

    TestFileSystem(URI uri, Path archive, Path tempFile) {
      super(java.nio.file.FileSystems.getDefault().provider(), uri, archive, "gzip", "entry", tempFile);
    }

    @Override
    protected void deregister(URI uri) {
      deregistered.add(uri);
    }

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return SingleEntryArchiveFormat.GZIP;
    }
  }

  @Test
  void closeDeregistersUriExactlyOnce() throws Exception {
    Path archive = Files.createTempFile("archive", ".gz");
    Path temp = Files.createTempFile("uncompressed", ".tmp");
    try {
      URI uri = URI.create("test:///some-archive");
      TestFileSystem fs = new TestFileSystem(uri, archive, temp);

      assertEquals(0, fs.deregistered.size());
      fs.close();

      assertFalse(fs.isOpen());
      assertFalse(Files.exists(temp));
      assertEquals(List.of(uri), fs.deregistered);

      fs.close();
      assertEquals(List.of(uri), fs.deregistered);
    } finally {
      Files.deleteIfExists(archive);
      Files.deleteIfExists(temp);
    }
  }
}
