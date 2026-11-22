package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class SimpleVirtualBasicAttributesMutaGate5Test {

  @Test
  void isSymbolicLinkIsFalseForDirectoryAndRegularFile() {
    SimpleVirtualBasicAttributes dir = SimpleVirtualBasicAttributes.directory("d");
    SimpleVirtualBasicAttributes file = SimpleVirtualBasicAttributes.regularFile("f", 5L);

    assertFalse(dir.isSymbolicLink());
    assertFalse(file.isSymbolicLink());
  }
}
