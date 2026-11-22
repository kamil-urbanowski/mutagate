package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BasicFileAttributesMapsMutaGate3Test {

  private static final class StubAttrs implements BasicFileAttributes {
    @Override
    public FileTime lastModifiedTime() {
      return FileTime.fromMillis(1000L);
    }

    @Override
    public FileTime lastAccessTime() {
      return FileTime.fromMillis(2000L);
    }

    @Override
    public FileTime creationTime() {
      return FileTime.fromMillis(3000L);
    }

    @Override
    public boolean isRegularFile() {
      return true;
    }

    @Override
    public boolean isDirectory() {
      return false;
    }

    @Override
    public boolean isSymbolicLink() {
      return false;
    }

    @Override
    public boolean isOther() {
      return false;
    }

    @Override
    public long size() {
      return 42L;
    }

    @Override
    public Object fileKey() {
      return null;
    }
  }

  @Test
  void explicitListPopulatesRequestedAttributesInOrder() {
    Map<String, Object> map =
        BasicFileAttributesMaps.basicAttributesToMap(new StubAttrs(), "size, isDirectory,,lastModifiedTime");

    assertEquals(3, map.size());
    assertEquals(42L, map.get("size"));
    assertEquals(false, map.get("isDirectory"));
    assertEquals(FileTime.fromMillis(1000L), map.get("lastModifiedTime"));
    assertEquals(
        java.util.List.of("size", "isDirectory", "lastModifiedTime"), new ArrayList<>(map.keySet()));
  }
}
