package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class TarFileStoreMutaGate1Test {

  @Test
  void getBlockSizeReturns512() throws IOException {
    TarFileStore store = new TarFileStore(null);
    assertEquals(512L, store.getBlockSize());
  }
}
