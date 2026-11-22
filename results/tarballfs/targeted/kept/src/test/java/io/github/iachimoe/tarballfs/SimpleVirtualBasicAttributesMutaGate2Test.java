package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class SimpleVirtualBasicAttributesMutaGate2Test {

  @Test
  void fileKeyReturnsEntryKeyForDirectoryAndRegularFile() {
    SimpleVirtualBasicAttributes dir = SimpleVirtualBasicAttributes.directory("dir/key");
    SimpleVirtualBasicAttributes file = SimpleVirtualBasicAttributes.regularFile("file/key", 42L);

    assertNotNull(dir.fileKey());
    assertNotNull(file.fileKey());
    assertEquals("dir/key", dir.fileKey());
    assertEquals("file/key", file.fileKey());
  }
}
