package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate11Test {

  @Test
  void notImplementedBuildsMessageFromProviderNameAndFeatureForEachFormat() {
    for (SingleEntryArchiveFormat format : SingleEntryArchiveFormat.values()) {
      UnsupportedOperationException ex =
          CompressorArchiveProviderSupport.notImplemented(format, "watch service");
      assertEquals(
          format.providerDisplayName() + " watch service is not implemented yet", ex.getMessage());
      assertNull(ex.getCause());
    }

    UnsupportedOperationException empty =
        CompressorArchiveProviderSupport.notImplemented(SingleEntryArchiveFormat.GZIP, "");
    assertEquals(
        SingleEntryArchiveFormat.GZIP.providerDisplayName() + "  is not implemented yet",
        empty.getMessage());
  }
}
