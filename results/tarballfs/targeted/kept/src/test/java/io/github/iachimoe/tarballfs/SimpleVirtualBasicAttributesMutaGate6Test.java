package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;

class SimpleVirtualBasicAttributesMutaGate6Test {

  @Test
  void lastAccessTimeIsEpochForDirectoryAndRegularFile() {
    FileTime epoch = FileTime.fromMillis(0L);

    SimpleVirtualBasicAttributes dir = SimpleVirtualBasicAttributes.directory("d/");
    assertNotNull(dir.lastAccessTime());
    assertEquals(epoch, dir.lastAccessTime());

    SimpleVirtualBasicAttributes file = SimpleVirtualBasicAttributes.regularFile("f", 5L);
    assertNotNull(file.lastAccessTime());
    assertEquals(epoch, file.lastAccessTime());
  }
}
