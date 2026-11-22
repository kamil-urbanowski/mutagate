package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;

class SimpleVirtualBasicAttributesMutaGate3Test {

  @Test
  void lastModifiedTimeIsEpochForDirectoryAndRegularFile() {
    FileTime epoch = FileTime.fromMillis(0L);

    SimpleVirtualBasicAttributes dir = SimpleVirtualBasicAttributes.directory("d/");
    SimpleVirtualBasicAttributes file = SimpleVirtualBasicAttributes.regularFile("f.txt", 42L);

    assertEquals(epoch, dir.lastModifiedTime());
    assertEquals(0L, dir.lastModifiedTime().toMillis());
    assertEquals(epoch, file.lastModifiedTime());
    assertEquals(0L, file.lastModifiedTime().toMillis());
  }
}
