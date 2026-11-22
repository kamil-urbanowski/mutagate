package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GzipBasicFileAttributeViewMutaGate1Test {

  @Test
  void nameReturnsBasic() {
    GzipBasicFileAttributeView view = new GzipBasicFileAttributeView(null);
    assertEquals("basic", view.name());
  }
}
