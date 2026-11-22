package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BasicFileAttributesMapsMutaGate1Test {

  private static final class FakeAttrs implements BasicFileAttributes {
    private final FileTime time = FileTime.fromMillis(1000L);

    @Override
    public FileTime lastModifiedTime() {
      return time;
    }

    @Override
    public FileTime lastAccessTime() {
      return time;
    }

    @Override
    public FileTime creationTime() {
      return time;
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
  void wildcardAndEmptyReturnAllWhileNamedReturnsOnlyRequested() {
    FakeAttrs attrs = new FakeAttrs();
    List<String> allKeys =
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
    assertEquals(allKeys, List.copyOf(star.keySet()));
    assertEquals(42L, star.get("size"));

    Map<String, Object> empty = BasicFileAttributesMaps.basicAttributesToMap(attrs, "");
    assertEquals(allKeys, List.copyOf(empty.keySet()));
    assertEquals(true, empty.get("isRegularFile"));

    Map<String, Object> named = BasicFileAttributesMaps.basicAttributesToMap(attrs, "size");
    assertEquals(List.of("size"), List.copyOf(named.keySet()));
    assertEquals(42L, named.get("size"));
  }
}
