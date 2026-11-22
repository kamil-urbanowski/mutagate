package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;

class SimpleVirtualBasicAttributesMutaGate1Test {

  @Test
  void creationTimeIsEpochForDirectoryAndRegularFile() {
    FileTime epoch = FileTime.fromMillis(0L);

    SimpleVirtualBasicAttributes dir = SimpleVirtualBasicAttributes.directory("d");
    assertNotNull(dir.creationTime());
    assertEquals(epoch, dir.creationTime());

    SimpleVirtualBasicAttributes file = SimpleVirtualBasicAttributes.regularFile("f", 5L);
    assertNotNull(file.creationTime());
    assertEquals(epoch, file.creationTime());
  }
}
