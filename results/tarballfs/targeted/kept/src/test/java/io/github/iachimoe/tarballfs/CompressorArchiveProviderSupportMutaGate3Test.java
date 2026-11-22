package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.attribute.FileOwnerAttributeView;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate3Test {

  @Test
  void nonBasicViewTypeReturnsNullWithoutInspectingPath() {
    FileOwnerAttributeView view =
        CompressorArchiveProviderSupport.basicFileAttributeViewOrNull(
            null, FileOwnerAttributeView.class, SingleEntryArchiveFormat.GZIP);
    assertNull(view);
  }
}
