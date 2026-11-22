package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate7Test {

  @Test
  void isSymbolicLinkReflectsEntryType() {
    TarBasicFileAttributes regular =
        TarBasicFileAttributes.fromEntry(new TarArchiveEntry("file.txt"), "file.txt");
    assertFalse(regular.isSymbolicLink());
    assertTrue(regular.isRegularFile());

    TarBasicFileAttributes dir =
        TarBasicFileAttributes.fromEntry(new TarArchiveEntry("dir/"), "dir/");
    assertFalse(dir.isSymbolicLink());

    TarBasicFileAttributes implied = TarBasicFileAttributes.impliedDirectory("implied/");
    assertFalse(implied.isSymbolicLink());

    TarBasicFileAttributes link =
        TarBasicFileAttributes.fromEntry(
            new TarArchiveEntry("link", TarConstants.LF_SYMLINK), "link");
    assertTrue(link.isSymbolicLink());
    assertFalse(link.isRegularFile());
  }
}
