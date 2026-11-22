package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate2Test {

  @Test
  void impliedDirectoryHasDirectoryAttributesEpochTimesAndKey() {
    String key = "some/dir/";
    TarBasicFileAttributes attrs = TarBasicFileAttributes.impliedDirectory(key);

    assertTrue(attrs.isDirectory());
    assertFalse(attrs.isRegularFile());
    assertFalse(attrs.isSymbolicLink());
    assertFalse(attrs.isOther());
    assertEquals(0L, attrs.size());

    FileTime epoch = FileTime.fromMillis(0L);
    assertEquals(epoch, attrs.lastModifiedTime());
    assertEquals(epoch, attrs.lastAccessTime());
    assertEquals(epoch, attrs.creationTime());

    assertSame(key, attrs.fileKey());
  }
}
