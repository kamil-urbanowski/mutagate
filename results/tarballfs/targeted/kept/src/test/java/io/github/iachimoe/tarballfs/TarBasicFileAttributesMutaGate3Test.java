package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate3Test {

  private static final class DivergingDatesEntry extends TarArchiveEntry {
    DivergingDatesEntry(String name) {
      super(name);
    }

    @Override
    public Date getLastModifiedDate() {
      return new Date(5000L);
    }

    @Override
    public Date getModTime() {
      return new Date(9000L);
    }
  }

  @Test
  void usesLastModifiedDateWhenPresent() {
    TarArchiveEntry entry = new DivergingDatesEntry("file.txt");

    TarBasicFileAttributes attrs = TarBasicFileAttributes.fromEntry(entry, "key");

    assertEquals(5000L, attrs.lastModifiedTime().toMillis());
    assertEquals(5000L, attrs.lastAccessTime().toMillis());
    assertEquals(5000L, attrs.creationTime().toMillis());
  }
}
