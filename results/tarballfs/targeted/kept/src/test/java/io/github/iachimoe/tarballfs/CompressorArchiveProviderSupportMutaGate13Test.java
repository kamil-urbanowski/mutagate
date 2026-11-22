package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate13Test {

  private static final class StubFileSystem extends AbstractCompressorArchiveFileSystem {
    private final SingleEntryArchiveFormat format;

    StubFileSystem(URI uri, Path host, Path temp, SingleEntryArchiveFormat format) {
      super(FileSystems.getDefault().provider(), uri, host, "stub", "virtual", temp);
      this.format = format;
    }

    @Override
    protected void deregister(URI uri) {}

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return format;
    }
  }

  @Test
  void pathFromUriExtractsEntryNameAfterBang() throws Exception {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    Path temp = Files.createTempFile("mutagate13", ".tmp");
    temp.toFile().deleteOnExit();
    URI fsUri = URI.create(format.uriSchemePrefix() + "/tmp/a.gz");
    StubFileSystem fs = new StubFileSystem(fsUri, temp, temp, format);

    URI uri = URI.create(format.uriSchemeId() + ":///tmp/a.gz!/inner/file.txt");
    Path result = CompressorArchiveProviderSupport.pathFromUri(uri, format, key -> fs);

    assertTrue(result instanceof SingleEntryArchivePath);
    SingleEntryArchivePath p = (SingleEntryArchivePath) result;
    assertEquals("inner/file.txt", p.entryName());
    assertTrue(p.isAbsolute());
  }
}
