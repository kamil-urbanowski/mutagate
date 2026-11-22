package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate11Test {

  @Test
  void notImplementedReturnsExceptionWithFormattedMessage() {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    UnsupportedOperationException ex =
        CompressorArchiveProviderSupport.notImplemented(format, "some feature");

    assertNotNull(ex);
    assertEquals(
        format.providerDisplayName() + " some feature is not implemented yet", ex.getMessage());
  }
}
