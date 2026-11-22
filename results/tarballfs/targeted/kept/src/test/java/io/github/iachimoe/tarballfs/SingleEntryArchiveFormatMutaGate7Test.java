package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate7Test {

  @Test
  void gzipWatchServiceMessageIsReturned() {
    assertEquals(
        "Watch service is not implemented for gzip entries",
        SingleEntryArchiveFormat.GZIP.watchServiceMessage());
  }
}
