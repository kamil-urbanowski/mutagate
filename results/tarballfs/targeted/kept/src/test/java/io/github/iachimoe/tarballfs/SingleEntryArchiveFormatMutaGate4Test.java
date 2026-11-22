package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate4Test {

  @Test
  void gzipToHostFileUnsupportedMessageIsExpected() {
    assertEquals(
        "Gzip entry path does not map to a host java.io.File",
        SingleEntryArchiveFormat.GZIP.toHostFileUnsupportedMessage());
  }
}
