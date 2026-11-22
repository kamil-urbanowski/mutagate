package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFileAttributeView;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate8Test {

  @Test
  void nonBasicViewTypesReturnNullWithoutValidatingPathOrFormat() {
    // The type check happens before the path is validated, so even a null path,
    // a foreign path, or a null format must not cause an exception.
    assertNull(CompressorArchiveProviderSupport.basicFileAttributeViewOrNull(
        null, PosixFileAttributeView.class, null));
    assertNull(CompressorArchiveProviderSupport.basicFileAttributeViewOrNull(
        null, FileOwnerAttributeView.class, null));
    assertNull(CompressorArchiveProviderSupport.basicFileAttributeViewOrNull(
        null, DosFileAttributeView.class, SingleEntryArchiveFormat.GZIP));
    assertNull(CompressorArchiveProviderSupport.basicFileAttributeViewOrNull(
        null, FileAttributeView.class, SingleEntryArchiveFormat.BZIP2));

    Path foreign = Path.of("some-file.gz");
    assertNull(CompressorArchiveProviderSupport.basicFileAttributeViewOrNull(
        foreign, PosixFileAttributeView.class, SingleEntryArchiveFormat.GZIP));
    assertNull(CompressorArchiveProviderSupport.basicFileAttributeViewOrNull(
        foreign, FileAttributeView.class, SingleEntryArchiveFormat.BZIP2));
  }
}
