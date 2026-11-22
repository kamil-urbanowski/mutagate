package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;

class SimpleVirtualBasicAttributesMutaGate5Test {

  @Test
  void creationTimeIsEpochForDirectoryAndRegularFile() {
    FileTime epoch = FileTime.fromMillis(0L);

    SimpleVirtualBasicAttributes dir = SimpleVirtualBasicAttributes.directory("d/");
    SimpleVirtualBasicAttributes file = SimpleVirtualBasicAttributes.regularFile("f.txt", 42L);

    assertEquals(epoch, dir.creationTime());
    assertEquals(epoch, file.creationTime());
    assertEquals(0L, dir.creationTime().toMillis());
    assertEquals(0L, file.creationTime().toMillis());
  }
}
