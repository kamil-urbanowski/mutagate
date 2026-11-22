package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate5Test {

  @Test
  void watchServiceMessagesAreFormatSpecific() {
    assertEquals(
        "Watch service is not implemented for gzip entries",
        SingleEntryArchiveFormat.GZIP.watchServiceMessage());
    assertEquals(
        "Watch service is not implemented for bzip2 entries",
        SingleEntryArchiveFormat.BZIP2.watchServiceMessage());
    assertNotEquals(
        SingleEntryArchiveFormat.GZIP.watchServiceMessage(),
        SingleEntryArchiveFormat.BZIP2.watchServiceMessage());
  }
}
