package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate4Test {

  private static final class NullLastModifiedEntry extends TarArchiveEntry {
    private final Date modTime;

    NullLastModifiedEntry(String name, Date modTime) {
      super(name);
      this.modTime = modTime;
    }

    @Override
    public Date getLastModifiedDate() {
      return null;
    }

    @Override
    public Date getModTime() {
      return modTime;
    }
  }

  @Test
  void usesModTimeWhenLastModifiedDateIsNull() {
    TarArchiveEntry entry = new NullLastModifiedEntry("file.txt", new Date(5000L));

    TarBasicFileAttributes attrs = TarBasicFileAttributes.fromEntry(entry, "key");

    assertEquals(5000L, attrs.lastModifiedTime().toMillis());
    assertEquals(5000L, attrs.lastAccessTime().toMillis());
    assertEquals(5000L, attrs.creationTime().toMillis());
  }
}
