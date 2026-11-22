package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate26Test {

  @Test
  void readOnlyFileSystemReturnsExceptionWithProviderNameMessage() {
    for (SingleEntryArchiveFormat format : SingleEntryArchiveFormat.values()) {
      UnsupportedOperationException ex = CompressorArchiveProviderSupport.readOnlyFileSystem(format);
      assertNotNull(ex);
      assertEquals(format.providerDisplayName() + " file system is read-only", ex.getMessage());
    }
  }
}
