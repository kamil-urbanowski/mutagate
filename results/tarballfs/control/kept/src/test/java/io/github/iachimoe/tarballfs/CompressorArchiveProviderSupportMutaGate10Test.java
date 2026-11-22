package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate10Test {

  @Test
  void readOnlyFileSystemBuildsMessageFromProviderDisplayNameForEachFormat() {
    for (SingleEntryArchiveFormat format : SingleEntryArchiveFormat.values()) {
      UnsupportedOperationException ex = CompressorArchiveProviderSupport.readOnlyFileSystem(format);
      assertNotNull(ex);
      assertEquals(format.providerDisplayName() + " file system is read-only", ex.getMessage());
      assertNull(ex.getCause());
    }
  }
}
