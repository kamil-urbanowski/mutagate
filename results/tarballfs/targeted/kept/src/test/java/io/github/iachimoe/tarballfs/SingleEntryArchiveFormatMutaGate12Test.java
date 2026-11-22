package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SingleEntryArchiveFormatMutaGate12Test {

  @Test
  void bzip2UriSchemeIdIsBzip2() {
    assertEquals("bzip2", SingleEntryArchiveFormat.BZIP2.uriSchemeId());
  }
}
