package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class AbstractCompressorArchiveFileSystemMutaGate10Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    TestFileSystem(URI uri, Path hostArchivePath, Path tempFile) {
      super(
          FileSystems.getDefault().provider(),
          uri,
          hostArchivePath,
          "gzip",
          "entry",
          tempFile);
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
  void hostArchivePathReturnsTheConfiguredPath() {
    Path archive = Paths.get("some-archive.gz");
    Path temp = Paths.get("some-archive.tmp");
    TestFileSystem fs = new TestFileSystem(URI.create("test:///archive"), archive, temp);

    assertNotNull(fs.hostArchivePath());
    assertSame(archive, fs.hostArchivePath());
  }
}
