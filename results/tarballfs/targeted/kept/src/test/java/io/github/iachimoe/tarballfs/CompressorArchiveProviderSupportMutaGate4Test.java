package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate4Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    TestFileSystem(URI uri, Path host, Path temp) {
      super(FileSystems.getDefault().provider(), uri, host, "test", "entry", temp);
    }

    @Override
    protected void deregister(URI uri) {}

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return SingleEntryArchiveFormat.GZIP;
    }
  }

  @Test
  void basicViewIsReturnedForBasicFileAttributeViewType() throws Exception {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    Path host = Files.createTempFile("host", ".gz");
    Path temp = Files.createTempFile("uncompressed", ".bin");
    try {
      URI uri = URI.create(format.uriSchemePrefix() + host.toAbsolutePath());
      TestFileSystem fs = new TestFileSystem(uri, host, temp);
      SingleEntryArchivePath path = new SingleEntryArchivePath(fs, format, true, "entry");

      BasicFileAttributeView view =
          CompressorArchiveProviderSupport.basicFileAttributeViewOrNull(
              path, BasicFileAttributeView.class, format);
      assertNotNull(view);
      assertTrue(view instanceof BasicFileAttributeView);

      FileAttributeView other =
          CompressorArchiveProviderSupport.basicFileAttributeViewOrNull(
              path, FileAttributeView.class, format);
      assertNull(other);
    } finally {
      Files.deleteIfExists(host);
      Files.deleteIfExists(temp);
    }
  }
}
