package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TarFileStoreMutaGate8Test {

  @Test
  void supportsFileAttributeViewByNameOnlyAcceptsBasic() {
    TarFileStore store = new TarFileStore(null);

    assertTrue(store.supportsFileAttributeView("basic"));
    assertFalse(store.supportsFileAttributeView("posix"));
    assertFalse(store.supportsFileAttributeView("owner"));
    assertFalse(store.supportsFileAttributeView(""));
    assertFalse(store.supportsFileAttributeView((String) null));
  }
}
