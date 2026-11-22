package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate10Test {

  @Test
  void bzip2UriSchemeIdIsPlainSchemeWithoutColonAndDiffersFromGzip() {
    String id = SingleEntryArchiveFormat.BZIP2.uriSchemeId();

    assertEquals("bzip2", id);
    assertEquals(
        SingleEntryArchiveFormat.BZIP2.uriSchemePrefix(), id + ":");
    assertNotEquals(SingleEntryArchiveFormat.GZIP.uriSchemeId(), id);
  }
}
