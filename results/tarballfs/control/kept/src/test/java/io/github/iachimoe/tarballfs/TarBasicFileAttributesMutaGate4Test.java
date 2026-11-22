package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.attribute.FileTime;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate4Test {

  @Test
  void lastAccessTimeMirrorsModificationTimeAndIsEpochForImpliedDirectory() {
    long millis = 1_700_000_000_000L;
    TarArchiveEntry entry = new TarArchiveEntry("dir/file.txt");
    entry.setModTime(millis);

    TarBasicFileAttributes fromEntry = TarBasicFileAttributes.fromEntry(entry, "dir/file.txt");
    assertEquals(FileTime.fromMillis(millis), fromEntry.lastAccessTime());
    assertEquals(fromEntry.lastModifiedTime(), fromEntry.lastAccessTime());
    assertEquals(fromEntry.creationTime(), fromEntry.lastAccessTime());

    TarBasicFileAttributes implied = TarBasicFileAttributes.impliedDirectory("dir/");
    assertEquals(FileTime.fromMillis(0L), implied.lastAccessTime());
  }
}
