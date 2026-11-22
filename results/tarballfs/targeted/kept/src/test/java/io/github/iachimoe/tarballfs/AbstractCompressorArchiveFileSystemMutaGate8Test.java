package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AbstractCompressorArchiveFileSystemMutaGate8Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    TestFileSystem(FileSystemProvider provider, URI uri, Path archive, Path temp) {
      super(provider, uri, archive, "gzip", "entry", temp);
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
  void getRootDirectoriesReturnsSingleAbsoluteRoot(@TempDir Path dir) throws Exception {
    Path archive = Files.createFile(dir.resolve("a.gz"));
    Path temp = dir.resolve("temp.bin");
    TestFileSystem fs = new TestFileSystem(
        FileSystems.getDefault().provider(), URI.create("test:///a"), archive, temp);

    List<Path> roots = new ArrayList<>();
    for (Path p : fs.getRootDirectories()) {
      roots.add(p);
    }

    assertEquals(1, roots.size());
    assertTrue(roots.get(0).isAbsolute());
    assertSame(fs, roots.get(0).getFileSystem());
  }
}
