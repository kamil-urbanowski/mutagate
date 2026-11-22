package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate13Test {

  @Test
  void toHostFileUnsupportedMessageIsFormatSpecific() {
    assertEquals(
        "Bzip2 entry path does not map to a host java.io.File",
        SingleEntryArchiveFormat.BZIP2.toHostFileUnsupportedMessage());
    assertEquals(
        "Gzip entry path does not map to a host java.io.File",
        SingleEntryArchiveFormat.GZIP.toHostFileUnsupportedMessage());
    assertNotEquals(
        SingleEntryArchiveFormat.GZIP.toHostFileUnsupportedMessage(),
        SingleEntryArchiveFormat.BZIP2.toHostFileUnsupportedMessage());
  }
}
