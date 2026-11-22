package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AbstractCompressorArchiveFileSystemMutaGate9Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    TestFileSystem() {
      super(
          FileSystems.getDefault().provider(),
          URI.create("test:///sep"),
          Path.of("archive.gz"),
          "gzip",
          "entry",
          Path.of("archive.gz.tmp"));
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
  void separatorIsForwardSlash() {
    TestFileSystem fs = new TestFileSystem();
    assertEquals("/", fs.getSeparator());
  }
}
