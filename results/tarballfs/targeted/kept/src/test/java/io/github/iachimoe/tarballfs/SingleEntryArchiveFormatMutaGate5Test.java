package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate5Test {

  @Test
  void gzipUriSchemeIdIsGzip() {
    assertEquals("gzip", SingleEntryArchiveFormat.GZIP.uriSchemeId());
  }
}
