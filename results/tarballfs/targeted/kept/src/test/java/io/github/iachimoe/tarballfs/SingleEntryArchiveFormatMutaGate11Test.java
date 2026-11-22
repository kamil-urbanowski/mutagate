package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate11Test {

  @Test
  void bzip2ToHostFileUnsupportedMessageIsExpected() {
    assertEquals(
        "Bzip2 entry path does not map to a host java.io.File",
        SingleEntryArchiveFormat.BZIP2.toHostFileUnsupportedMessage());
  }
}
