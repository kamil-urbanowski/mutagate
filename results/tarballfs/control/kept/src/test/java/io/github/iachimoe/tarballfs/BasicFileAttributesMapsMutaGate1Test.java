package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BasicFileAttributesMapsMutaGate1Test {

  private static final class StubAttrs implements BasicFileAttributes {
    static final FileTime MODIFIED = FileTime.fromMillis(1000L);
    static final FileTime ACCESSED = FileTime.fromMillis(2000L);
    static final FileTime CREATED = FileTime.fromMillis(3000L);
    static final Object KEY = new Object();

    @Override
    public FileTime lastModifiedTime() {
      return MODIFIED;
    }

    @Override
    public FileTime lastAccessTime() {
      return ACCESSED;
    }

    @Override
    public FileTime creationTime() {
      return CREATED;
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
      return KEY;
    }
  }

  @Test
  void convertsAttributesForAllAndSelectedLists() {
    StubAttrs attrs = new StubAttrs();

    List<String> expectedOrder =
        List.of(
            "lastModifiedTime",
            "lastAccessTime",
            "creationTime",
            "size",
            "isRegularFile",
            "isDirectory",
            "isSymbolicLink",
            "isOther",
            "fileKey");

    Map<String, Object> star = BasicFileAttributesMaps.basicAttributesToMap(attrs, "*");
    assertEquals(expectedOrder, new ArrayList<>(star.keySet()));
    assertEquals(StubAttrs.MODIFIED, star.get("lastModifiedTime"));
    assertEquals(StubAttrs.ACCESSED, star.get("lastAccessTime"));
    assertEquals(StubAttrs.CREATED, star.get("creationTime"));
    assertEquals(42L, star.get("size"));
    assertEquals(true, star.get("isRegularFile"));
    assertEquals(false, star.get("isDirectory"));
    assertEquals(false, star.get("isSymbolicLink"));
    assertEquals(false, star.get("isOther"));
    assertTrue(star.get("fileKey") == StubAttrs.KEY);

    Map<String, Object> empty = BasicFileAttributesMaps.basicAttributesToMap(attrs, "");
    assertEquals(expectedOrder, new ArrayList<>(empty.keySet()));

    Map<String, Object> selected =
        BasicFileAttributesMaps.basicAttributesToMap(attrs, " size , ,isDirectory,size,creationTime");
    assertEquals(List.of("size", "isDirectory", "creationTime"), new ArrayList<>(selected.keySet()));
    assertEquals(42L, selected.get("size"));
    assertEquals(false, selected.get("isDirectory"));
    assertEquals(StubAttrs.CREATED, selected.get("creationTime"));

    Map<String, Object> onlySeparators = BasicFileAttributesMaps.basicAttributesToMap(attrs, " , ,");
    assertTrue(onlySeparators.isEmpty());

    IllegalArgumentException unknown =
        assertThrows(
            IllegalArgumentException.class,
            () -> BasicFileAttributesMaps.basicAttributesToMap(attrs, "size,bogus"));
    assertEquals("Unknown basic attribute: bogus", unknown.getMessage());

    IllegalArgumentException starInList =
        assertThrows(
            IllegalArgumentException.class,
            () -> BasicFileAttributesMaps.basicAttributesToMap(attrs, "size,*"));
    assertEquals("Unknown basic attribute: *", starInList.getMessage());
  }
}
