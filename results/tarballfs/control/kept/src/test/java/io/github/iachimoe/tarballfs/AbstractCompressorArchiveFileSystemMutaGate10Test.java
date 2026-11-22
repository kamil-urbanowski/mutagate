package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AbstractCompressorArchiveFileSystemMutaGate10Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    int deregisterCount;

    TestFileSystem(URI uri, Path archive, Path temp) {
      super(FileSystems.getDefault().provider(), uri, archive, "gzip", "entry", temp);
    }

    @Override
    protected void deregister(URI uri) {
      deregisterCount++;
    }

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return SingleEntryArchiveFormat.GZIP;
    }
  }

  @Test
  void separatorIsSlashBeforeAndAfterClose() throws Exception {
    Path archive = Path.of("nonexistent-archive.gz");
    Path temp = Path.of(System.getProperty("java.io.tmpdir"), "mutagate10-nonexistent-" + System.nanoTime() + ".tmp");
    TestFileSystem fs = new TestFileSystem(URI.create("gzip:file:///nonexistent-archive.gz"), archive, temp);

    String before = fs.getSeparator();
    assertEquals("/", before);
    assertEquals(1, before.length());
    assertEquals('/', before.charAt(0));
    assertFalse(before.isEmpty());

    fs.close();
    assertFalse(fs.isOpen());
    assertEquals(1, fs.deregisterCount);

    assertEquals("/", fs.getSeparator());
  }
}
