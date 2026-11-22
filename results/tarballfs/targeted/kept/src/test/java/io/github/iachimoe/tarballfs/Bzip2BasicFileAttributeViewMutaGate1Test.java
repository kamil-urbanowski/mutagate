package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class Bzip2BasicFileAttributeViewMutaGate1Test {

  @Test
  void nameReturnsBasic() {
    Bzip2BasicFileAttributeView view = new Bzip2BasicFileAttributeView(null);
    assertEquals("basic", view.name());
  }
}
