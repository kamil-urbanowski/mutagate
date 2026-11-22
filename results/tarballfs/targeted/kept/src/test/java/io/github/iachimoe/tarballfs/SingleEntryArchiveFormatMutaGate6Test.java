package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate6Test {

  @Test
  void gzipUriSchemePrefixIsGzipColon() {
    assertEquals("gzip:", SingleEntryArchiveFormat.GZIP.uriSchemePrefix());
  }
}
