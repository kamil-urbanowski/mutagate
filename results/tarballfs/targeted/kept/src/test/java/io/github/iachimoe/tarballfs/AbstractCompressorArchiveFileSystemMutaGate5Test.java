package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.ClosedFileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.spi.FileSystemProvider;
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
  void getPathMatcherWorksWhenOpenAndFailsWhenClosed() throws IOException {
    Path temp = Files.createTempFile("mutagate5", ".tmp");
    TestFileSystem fs = new TestFileSystem(
        FileSystems.getDefault().provider(),
        URI.create("test:///archive"),
        Path.of("archive.gz"),
        temp);
    try {
      PathMatcher matcher = fs.getPathMatcher("glob:*.txt");
      assertNotNull(matcher);
      assertTrue(matcher.matches(Path.of("a.txt")));
      assertFalse(matcher.matches(Path.of("a.bin")));
    } finally {
      fs.close();
    }

    assertThrows(ClosedFileSystemException.class, () -> fs.getPathMatcher("glob:*.txt"));
  }
}
