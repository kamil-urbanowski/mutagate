package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AbstractCompressorArchiveFileSystemMutaGate13Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    TestFileSystem() {
      super(
          FileSystems.getDefault().provider(),
          URI.create("test:///archive"),
          Path.of("archive.gz"),
          "gzip",
          "entry",
          Path.of("archive.tmp"));
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
  void supportedFileAttributeViewsContainsOnlyBasic() {
    TestFileSystem fs = new TestFileSystem();
    Set<String> views = fs.supportedFileAttributeViews();
    assertEquals(Set.of("basic"), views);
  }
}
