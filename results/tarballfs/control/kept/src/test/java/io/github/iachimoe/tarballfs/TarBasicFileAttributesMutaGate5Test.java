package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.attribute.FileTime;
import java.util.Date;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate5Test {

  @Test
  void creationTimeMatchesModifiedTimeForEntryAndEpochForImpliedDirectory() {
    TarArchiveEntry entry = new TarArchiveEntry("file.txt");
    entry.setModTime(new Date(123456000L));

    TarBasicFileAttributes attrs = TarBasicFileAttributes.fromEntry(entry, "file.txt");
    assertEquals(FileTime.fromMillis(123456000L), attrs.creationTime());
    assertEquals(attrs.lastModifiedTime(), attrs.creationTime());

    TarBasicFileAttributes implied = TarBasicFileAttributes.impliedDirectory("dir/");
    assertEquals(FileTime.fromMillis(0L), implied.creationTime());
  }
}
