package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class SimpleVirtualBasicAttributesMutaGate4Test {

  @Test
  void isOtherIsFalseForDirectoryAndRegularFile() {
    assertFalse(SimpleVirtualBasicAttributes.directory("dir/").isOther());
    assertFalse(SimpleVirtualBasicAttributes.regularFile("file.txt", 10L).isOther());
  }
}
