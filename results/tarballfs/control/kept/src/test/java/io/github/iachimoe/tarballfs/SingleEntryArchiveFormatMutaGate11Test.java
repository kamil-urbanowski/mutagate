package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate11Test {

  @Test
  void bzip2ProviderDisplayNameIsBzip2AndDiffersFromGzip() {
    assertEquals("Bzip2", SingleEntryArchiveFormat.BZIP2.providerDisplayName());
    assertNotEquals(
        SingleEntryArchiveFormat.GZIP.providerDisplayName(),
        SingleEntryArchiveFormat.BZIP2.providerDisplayName());
  }
}
