package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate14Test {

  @Test
  void bzip2WatchServiceMessageIsExpected() {
    assertEquals(
        "Watch service is not implemented for bzip2 entries",
        SingleEntryArchiveFormat.BZIP2.watchServiceMessage());
  }
}
