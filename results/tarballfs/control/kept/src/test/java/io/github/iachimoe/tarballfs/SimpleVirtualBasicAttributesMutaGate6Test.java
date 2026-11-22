package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SimpleVirtualBasicAttributesMutaGate6Test {

  @Test
  void isRegularFileReflectsFactoryUsed() {
    SimpleVirtualBasicAttributes file = SimpleVirtualBasicAttributes.regularFile("a/b.txt", 42L);
    SimpleVirtualBasicAttributes emptyFile = SimpleVirtualBasicAttributes.regularFile("empty", 0L);
    SimpleVirtualBasicAttributes dir = SimpleVirtualBasicAttributes.directory("a/");

    assertTrue(file.isRegularFile());
    assertTrue(emptyFile.isRegularFile());
    assertFalse(dir.isRegularFile());

    assertFalse(file.isDirectory());
    assertTrue(dir.isDirectory());
  }
}
