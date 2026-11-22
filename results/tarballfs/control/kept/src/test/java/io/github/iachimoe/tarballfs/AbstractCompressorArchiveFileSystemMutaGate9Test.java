package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class AbstractCompressorArchiveFileSystemMutaGate9Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    final List<URI> deregistered = new ArrayList<>();

    TestFileSystem(URI uri, Path archive, Path temp) {
      super(FileSystems.getDefault().provider(), uri, archive, "gzip", "entry", temp);
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
  void isReadOnlyIsTrueWhileOpenAndAfterClose() throws IOException {
    URI uri = URI.create("test:/readonly");
    Path tmpDir = Path.of(System.getProperty("java.io.tmpdir"));
    Path archive = tmpDir.resolve("mutagate9-nonexistent-archive.gz");
    Path temp = tmpDir.resolve("mutagate9-nonexistent-temp.bin");
    TestFileSystem fs = new TestFileSystem(uri, archive, temp);

    assertTrue(fs.isOpen());
    assertTrue(fs.isReadOnly());

    fs.close();

    assertFalse(fs.isOpen());
    assertTrue(fs.isReadOnly());
    assertEquals(List.of(uri), fs.deregistered);
  }
}
