package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate8Test {

  @Test
  void isSymbolicLinkReflectsEntryType() {
    TarArchiveEntry symlinkEntry = new TarArchiveEntry("link", TarConstants.LF_SYMLINK);
    symlinkEntry.setLinkName("target");
    TarArchiveEntry fileEntry = new TarArchiveEntry("file.txt");
    TarArchiveEntry dirEntry = new TarArchiveEntry("dir/");

    TarBasicFileAttributes symlink = TarBasicFileAttributes.fromEntry(symlinkEntry, "link");
    TarBasicFileAttributes file = TarBasicFileAttributes.fromEntry(fileEntry, "file.txt");
    TarBasicFileAttributes dir = TarBasicFileAttributes.fromEntry(dirEntry, "dir/");
    TarBasicFileAttributes implied = TarBasicFileAttributes.impliedDirectory("implied/");

    assertTrue(symlink.isSymbolicLink());
    assertFalse(symlink.isRegularFile());
    assertFalse(symlink.isDirectory());

    assertFalse(file.isSymbolicLink());
    assertFalse(dir.isSymbolicLink());
    assertFalse(implied.isSymbolicLink());
  }
}
