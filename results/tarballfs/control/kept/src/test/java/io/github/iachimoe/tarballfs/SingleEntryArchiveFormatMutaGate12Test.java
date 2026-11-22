package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate12Test {

  @Test
  void bzip2WatchServiceMessageIsExactAndDistinctFromGzip() {
    String bzip2 = SingleEntryArchiveFormat.BZIP2.watchServiceMessage();
    assertEquals("Watch service is not implemented for bzip2 entries", bzip2);
    assertNotEquals(SingleEntryArchiveFormat.GZIP.watchServiceMessage(), bzip2);
    assertEquals("Watch service is not implemented for gzip entries",
        SingleEntryArchiveFormat.GZIP.watchServiceMessage());
  }
}
