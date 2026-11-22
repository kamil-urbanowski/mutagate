package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate13Test {

  @Test
  void bzip2UriSchemePrefixIsBzip2Colon() {
    assertEquals("bzip2:", SingleEntryArchiveFormat.BZIP2.uriSchemePrefix());
  }
}
