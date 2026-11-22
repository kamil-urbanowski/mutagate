package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate2Test {

  @Test
  void uriSchemePrefixIsSchemeIdPlusColonAndDistinctPerFormat() {
    String gzipPrefix = SingleEntryArchiveFormat.GZIP.uriSchemePrefix();
    String bzip2Prefix = SingleEntryArchiveFormat.BZIP2.uriSchemePrefix();

    assertEquals("gzip:", gzipPrefix);
    assertEquals(SingleEntryArchiveFormat.GZIP.uriSchemeId() + ":", gzipPrefix);
    assertTrue(gzipPrefix.endsWith(":"));
    assertEquals(5, gzipPrefix.length());
    assertNotEquals(bzip2Prefix, gzipPrefix);
    assertEquals("bzip2:", bzip2Prefix);
  }
}
