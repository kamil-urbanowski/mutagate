package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate6Test {

  @Test
  void symbolicLinkEntryReportsSymbolicLink() {
    TarArchiveEntry linkEntry = new TarArchiveEntry("link", TarConstants.LF_SYMLINK);
    linkEntry.setLinkName("target");
    TarBasicFileAttributes linkAttrs = TarBasicFileAttributes.fromEntry(linkEntry, "link");

    assertTrue(linkAttrs.isSymbolicLink());
    assertFalse(linkAttrs.isRegularFile());
    assertFalse(linkAttrs.isDirectory());

    TarArchiveEntry fileEntry = new TarArchiveEntry("file.txt");
    TarBasicFileAttributes fileAttrs = TarBasicFileAttributes.fromEntry(fileEntry, "file.txt");
    assertFalse(fileAttrs.isSymbolicLink());
  }
}
