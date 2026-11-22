package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.attribute.FileTime;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate1Test {

  @Test
  void fromEntryMapsTypeSizeTimesAndKeyForFileDirectoryAndSymlink() {
    long millis = 1_700_000_000_000L;

    TarArchiveEntry fileEntry = new TarArchiveEntry("dir/file.txt");
    fileEntry.setSize(1234L);
    fileEntry.setModTime(millis);
    String fileKey = "dir/file.txt";
    TarBasicFileAttributes fileAttrs = TarBasicFileAttributes.fromEntry(fileEntry, fileKey);

    assertTrue(fileAttrs.isRegularFile());
    assertFalse(fileAttrs.isDirectory());
    assertFalse(fileAttrs.isSymbolicLink());
    assertFalse(fileAttrs.isOther());
    assertEquals(1234L, fileAttrs.size());
    assertEquals(FileTime.fromMillis(millis), fileAttrs.lastModifiedTime());
    assertEquals(FileTime.fromMillis(millis), fileAttrs.lastAccessTime());
    assertEquals(FileTime.fromMillis(millis), fileAttrs.creationTime());
    assertSame(fileKey, fileAttrs.fileKey());

    TarArchiveEntry dirEntry = new TarArchiveEntry("dir/");
    dirEntry.setSize(100L);
    dirEntry.setModTime(millis);
    String dirKey = "dir/";
    TarBasicFileAttributes dirAttrs = TarBasicFileAttributes.fromEntry(dirEntry, dirKey);

    assertFalse(dirAttrs.isRegularFile());
    assertTrue(dirAttrs.isDirectory());
    assertFalse(dirAttrs.isSymbolicLink());
    assertFalse(dirAttrs.isOther());
    assertEquals(0L, dirAttrs.size());
    assertEquals(FileTime.fromMillis(millis), dirAttrs.lastModifiedTime());
    assertSame(dirKey, dirAttrs.fileKey());

    TarArchiveEntry linkEntry = new TarArchiveEntry("link", TarConstants.LF_SYMLINK);
    linkEntry.setLinkName("target");
    linkEntry.setSize(5L);
    linkEntry.setModTime(millis);
    String linkKey = "link";
    TarBasicFileAttributes linkAttrs = TarBasicFileAttributes.fromEntry(linkEntry, linkKey);

    assertFalse(linkAttrs.isRegularFile());
    assertFalse(linkAttrs.isDirectory());
    assertTrue(linkAttrs.isSymbolicLink());
    assertFalse(linkAttrs.isOther());
    assertEquals(5L, linkAttrs.size());
    assertEquals(FileTime.fromMillis(millis), linkAttrs.lastModifiedTime());
    assertEquals(FileTime.fromMillis(millis), linkAttrs.lastAccessTime());
    assertEquals(FileTime.fromMillis(millis), linkAttrs.creationTime());
    assertSame(linkKey, linkAttrs.fileKey());
  }
}
