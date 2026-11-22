package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AbstractCompressorArchiveFileSystemMutaGate11Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    TestFileSystem(Path archive, Path temp) {
      super(
          FileSystems.getDefault().provider(),
          URI.create("test:/mutagate11"),
          archive,
          "gzip",
          "entry",
          temp);
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
  void supportedFileAttributeViewsIsImmutableBasicOnlyAndAvailableAfterClose() throws Exception {
    Path tmpDir = Path.of(System.getProperty("java.io.tmpdir"));
    Path archive = tmpDir.resolve("mutagate11-" + UUID.randomUUID() + ".gz");
    Path temp = tmpDir.resolve("mutagate11-" + UUID.randomUUID() + ".tmp");
    TestFileSystem fs = new TestFileSystem(archive, temp);

    Set<String> views = fs.supportedFileAttributeViews();
    assertEquals(Set.of("basic"), views);
    assertEquals(1, views.size());
    assertTrue(views.contains("basic"));
    assertTrue(!views.contains("posix"));
    assertTrue(!views.contains("unix"));
    assertTrue(!views.contains("owner"));
    assertThrows(UnsupportedOperationException.class, () -> views.add("posix"));
    assertThrows(UnsupportedOperationException.class, () -> views.remove("basic"));
    assertThrows(UnsupportedOperationException.class, views::clear);

    fs.close();
    assertTrue(!fs.isOpen());
    assertEquals(Set.of("basic"), fs.supportedFileAttributeViews());
  }
}
