package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

class AbstractCompressorArchiveFileSystemMutaGate7Test {

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

  @Test
  void getRootDirectoriesWorksWhenOpenAndThrowsWhenClosed() throws IOException {
    Path archive = Files.createTempFile("mutagate7-archive", ".gz");
    Path temp = Files.createTempFile("mutagate7-temp", ".tmp");
    try {
      TestFileSystem fs =
          new TestFileSystem(
              FileSystems.getDefault().provider(), URI.create("mutagate7:test"), archive, temp);

      List<Path> roots = new ArrayList<>();
      for (Path p : fs.getRootDirectories()) {
        roots.add(p);
      }
      assertEquals(1, roots.size());
      assertSame(fs, roots.get(0).getFileSystem());

      fs.close();
      assertThrows(ClosedFileSystemException.class, fs::getRootDirectories);
    } finally {
      Files.deleteIfExists(archive);
      Files.deleteIfExists(temp);
    }
  }
}
