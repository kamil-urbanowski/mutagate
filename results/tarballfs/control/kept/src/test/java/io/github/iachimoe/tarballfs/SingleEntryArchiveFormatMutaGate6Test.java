package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate6Test {

  @Test
  void toHostFileUnsupportedMessageIsFormatSpecific() {
    assertEquals(
        "Gzip entry path does not map to a host java.io.File",
        SingleEntryArchiveFormat.GZIP.toHostFileUnsupportedMessage());
    assertEquals(
        "Bzip2 entry path does not map to a host java.io.File",
        SingleEntryArchiveFormat.BZIP2.toHostFileUnsupportedMessage());
    assertNotEquals(
        SingleEntryArchiveFormat.GZIP.toHostFileUnsupportedMessage(),
        SingleEntryArchiveFormat.BZIP2.toHostFileUnsupportedMessage());
  }
}
