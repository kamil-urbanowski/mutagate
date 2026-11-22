package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AbstractCompressorArchiveFileSystemMutaGate12Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    TestFileSystem(Path archive, Path temp) {
      super(
          FileSystems.getDefault().provider(),
          URI.create("test:/gate12"),
          archive,
          "gzip",
          "entry",
          temp);
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
  void userPrincipalLookupServiceIsUnsupportedEvenAfterClose() throws Exception {
    Path archive = Path.of("gate12-nonexistent-archive.gz");
    Path temp = Path.of("gate12-nonexistent-temp.tmp");
    TestFileSystem fs = new TestFileSystem(archive, temp);

    UnsupportedOperationException openEx =
        assertThrows(UnsupportedOperationException.class, fs::getUserPrincipalLookupService);
    assertEquals("User principal lookup is not implemented yet", openEx.getMessage());

    fs.close();
    assertFalse(fs.isOpen());

    UnsupportedOperationException closedEx =
        assertThrows(UnsupportedOperationException.class, fs::getUserPrincipalLookupService);
    assertEquals("User principal lookup is not implemented yet", closedEx.getMessage());
  }
}
