package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SimpleVirtualBasicAttributesMutaGate3Test {

  @Test
  void isDirectoryReflectsEntryType() {
    SimpleVirtualBasicAttributes dir = SimpleVirtualBasicAttributes.directory("d");
    SimpleVirtualBasicAttributes file = SimpleVirtualBasicAttributes.regularFile("f", 5L);

    assertTrue(dir.isDirectory());
    assertFalse(file.isDirectory());
  }
}
