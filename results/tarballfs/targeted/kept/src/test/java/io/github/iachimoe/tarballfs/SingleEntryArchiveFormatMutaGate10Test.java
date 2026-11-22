package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate10Test {

  @Test
  void bzip2ProviderDisplayNameIsBzip2() {
    assertEquals("Bzip2", SingleEntryArchiveFormat.BZIP2.providerDisplayName());
  }
}
