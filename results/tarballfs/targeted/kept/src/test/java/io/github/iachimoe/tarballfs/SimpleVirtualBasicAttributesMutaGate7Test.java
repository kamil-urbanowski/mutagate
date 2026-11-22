package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;

class SimpleVirtualBasicAttributesMutaGate7Test {

  @Test
  void lastModifiedTimeIsEpochForDirectoryAndRegularFile() {
    FileTime epoch = FileTime.fromMillis(0L);

    SimpleVirtualBasicAttributes dir = SimpleVirtualBasicAttributes.directory("d");
    assertNotNull(dir.lastModifiedTime());
    assertEquals(epoch, dir.lastModifiedTime());

    SimpleVirtualBasicAttributes file = SimpleVirtualBasicAttributes.regularFile("f", 5L);
    assertNotNull(file.lastModifiedTime());
    assertEquals(epoch, file.lastModifiedTime());
  }
}
