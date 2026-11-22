package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;

class SimpleVirtualBasicAttributesMutaGate2Test {

  @Test
  void regularFileHasExpectedAttributes() {
    String key = "dir/file.txt";
    SimpleVirtualBasicAttributes attrs = SimpleVirtualBasicAttributes.regularFile(key, 12345L);

    assertTrue(attrs.isRegularFile());
    assertFalse(attrs.isDirectory());
    assertFalse(attrs.isSymbolicLink());
    assertFalse(attrs.isOther());
    assertEquals(12345L, attrs.size());
    assertSame(key, attrs.fileKey());

    FileTime epoch = FileTime.fromMillis(0L);
    assertEquals(epoch, attrs.lastModifiedTime());
    assertEquals(epoch, attrs.lastAccessTime());
    assertEquals(epoch, attrs.creationTime());
  }
}
