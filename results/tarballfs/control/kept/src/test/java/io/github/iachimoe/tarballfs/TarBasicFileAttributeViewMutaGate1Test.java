package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TarBasicFileAttributeViewMutaGate1Test {

  @Test
  void nameReturnsBasicRegardlessOfPath() {
    TarBasicFileAttributeView view = new TarBasicFileAttributeView(null);

    assertEquals("basic", view.name());
    assertEquals("basic", view.name());
  }
}
