package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate7Test {

  private static final class StubFileSystem extends AbstractCompressorArchiveFileSystem {
    private final SingleEntryArchiveFormat format;

    StubFileSystem(SingleEntryArchiveFormat format) {
      super(
          FileSystems.getDefault().provider(),
          URI.create("stub:/tmp/stub.gz"),
          Path.of("stub.gz"),
          "stub",
          "stub",
          Path.of("stub"));
      this.format = format;
    }

    @Override
    protected void deregister(URI uri) {
      // Nothing to deregister.
    }

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return format;
    }
  }

  @Test
  void checkReadAccessAllowsReadAndDeniesOtherModes() {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    StubFileSystem fs = new StubFileSystem(format);
    SingleEntryArchivePath root = new SingleEntryArchivePath(fs, format, true, "");

    assertDoesNotThrow(
        () -> CompressorArchiveProviderSupport.checkReadAccess(
            root, new AccessMode[] {AccessMode.READ}, format));

    assertThrows(
        AccessDeniedException.class,
        () -> CompressorArchiveProviderSupport.checkReadAccess(
            root, new AccessMode[] {AccessMode.WRITE}, format));
  }
}
