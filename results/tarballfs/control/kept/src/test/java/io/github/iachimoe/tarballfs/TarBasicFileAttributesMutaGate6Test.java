package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate6Test {

  @Test
  void isRegularFileDistinguishesFilesDirectoriesSymlinksAndImpliedDirectories() {
    TarArchiveEntry fileEntry = new TarArchiveEntry("file.txt");
    fileEntry.setSize(42L);
    TarBasicFileAttributes fileAttrs = TarBasicFileAttributes.fromEntry(fileEntry, "file.txt");
    assertTrue(fileAttrs.isRegularFile());
    assertFalse(fileAttrs.isDirectory());
    assertFalse(fileAttrs.isSymbolicLink());

    TarArchiveEntry dirEntry = new TarArchiveEntry("dir/");
    TarBasicFileAttributes dirAttrs = TarBasicFileAttributes.fromEntry(dirEntry, "dir/");
    assertFalse(dirAttrs.isRegularFile());
    assertTrue(dirAttrs.isDirectory());

    TarArchiveEntry linkEntry = new TarArchiveEntry("link", TarConstants.LF_SYMLINK);
    linkEntry.setLinkName("file.txt");
    TarBasicFileAttributes linkAttrs = TarBasicFileAttributes.fromEntry(linkEntry, "link");
    assertFalse(linkAttrs.isRegularFile());
    assertTrue(linkAttrs.isSymbolicLink());

    TarBasicFileAttributes implied = TarBasicFileAttributes.impliedDirectory("implied/");
    assertFalse(implied.isRegularFile());
    assertTrue(implied.isDirectory());
  }
}
