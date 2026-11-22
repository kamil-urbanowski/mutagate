package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate3Test {

  @Test
  void uriSchemeIdIsBareSchemeConsistentWithPrefixAndDistinctPerFormat() {
    String gzipId = SingleEntryArchiveFormat.GZIP.uriSchemeId();
    String bzip2Id = SingleEntryArchiveFormat.BZIP2.uriSchemeId();

    assertEquals("gzip", gzipId);
    assertEquals("bzip2", bzip2Id);
    assertNotEquals(gzipId, bzip2Id);

    assertEquals(gzipId + ":", SingleEntryArchiveFormat.GZIP.uriSchemePrefix());
    assertEquals(bzip2Id + ":", SingleEntryArchiveFormat.BZIP2.uriSchemePrefix());

    assertTrue(!gzipId.isEmpty());
    assertEquals(gzipId, gzipId.toLowerCase());
    assertEquals(gzipId, gzipId.trim());
  }
}
