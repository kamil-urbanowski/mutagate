package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;

class SimpleVirtualBasicAttributesMutaGate4Test {

  @Test
  void lastAccessTimeIsEpochForDirectoryAndRegularFile() {
    FileTime epoch = FileTime.fromMillis(0L);

    SimpleVirtualBasicAttributes dir = SimpleVirtualBasicAttributes.directory("d");
    SimpleVirtualBasicAttributes file = SimpleVirtualBasicAttributes.regularFile("f", 42L);

    assertNotNull(dir.lastAccessTime());
    assertNotNull(file.lastAccessTime());
    assertEquals(epoch, dir.lastAccessTime());
    assertEquals(epoch, file.lastAccessTime());
    assertEquals(0L, dir.lastAccessTime().toMillis());
    assertEquals(0L, file.lastAccessTime().toMillis());
  }
}
