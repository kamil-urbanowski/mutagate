package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate7Test {

  @Test
  void isDirectoryReflectsEntryType() {
    TarBasicFileAttributes dir =
        TarBasicFileAttributes.fromEntry(new TarArchiveEntry("a/dir/"), "a/dir/");
    assertTrue(dir.isDirectory());
    assertFalse(dir.isRegularFile());
    assertFalse(dir.isSymbolicLink());

    TarBasicFileAttributes file =
        TarBasicFileAttributes.fromEntry(new TarArchiveEntry("a/file.txt"), "a/file.txt");
    assertFalse(file.isDirectory());
    assertTrue(file.isRegularFile());

    TarBasicFileAttributes link =
        TarBasicFileAttributes.fromEntry(
            new TarArchiveEntry("a/link", TarArchiveEntry.LF_SYMLINK), "a/link");
    assertFalse(link.isDirectory());
    assertTrue(link.isSymbolicLink());

    TarBasicFileAttributes implied = TarBasicFileAttributes.impliedDirectory("a/");
    assertTrue(implied.isDirectory());
    assertFalse(implied.isRegularFile());
  }
}
