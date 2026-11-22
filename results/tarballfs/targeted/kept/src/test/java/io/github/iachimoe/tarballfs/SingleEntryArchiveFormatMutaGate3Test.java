package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate3Test {

  @Test
  void gzipProviderDisplayNameIsGzip() {
    assertEquals("Gzip", SingleEntryArchiveFormat.GZIP.providerDisplayName());
  }
}
