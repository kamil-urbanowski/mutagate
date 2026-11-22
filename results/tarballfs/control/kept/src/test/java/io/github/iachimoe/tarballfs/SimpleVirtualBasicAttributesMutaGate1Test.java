package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;

class SimpleVirtualBasicAttributesMutaGate1Test {

  @Test
  void directoryAttributesHaveExpectedValues() {
    String key = "some/dir/";
    SimpleVirtualBasicAttributes attrs = SimpleVirtualBasicAttributes.directory(key);

    FileTime epoch = FileTime.fromMillis(0L);

    assertTrue(attrs.isDirectory());
    assertFalse(attrs.isRegularFile());
    assertFalse(attrs.isSymbolicLink());
    assertFalse(attrs.isOther());
    assertEquals(0L, attrs.size());
    assertEquals(epoch, attrs.lastModifiedTime());
    assertEquals(epoch, attrs.lastAccessTime());
    assertEquals(epoch, attrs.creationTime());
    assertEquals(key, attrs.fileKey());
  }
}
