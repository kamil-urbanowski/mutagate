package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate9Test {

  @Test
  void bzip2UriSchemePrefixIsSchemeIdWithColonAndDiffersFromGzip() {
    String prefix = SingleEntryArchiveFormat.BZIP2.uriSchemePrefix();

    assertEquals("bzip2:", prefix);
    assertEquals(SingleEntryArchiveFormat.BZIP2.uriSchemeId() + ":", prefix);
    assertNotEquals(SingleEntryArchiveFormat.GZIP.uriSchemePrefix(), prefix);
  }
}
