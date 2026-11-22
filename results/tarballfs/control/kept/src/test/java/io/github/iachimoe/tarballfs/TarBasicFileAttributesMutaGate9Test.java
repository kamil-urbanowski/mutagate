package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate9Test {

  @Test
  void isOtherIsFalseForAllKindsOfAttributes() {
    TarBasicFileAttributes file =
        TarBasicFileAttributes.fromEntry(new TarArchiveEntry("a.txt"), "a.txt");
    TarBasicFileAttributes dir =
        TarBasicFileAttributes.fromEntry(new TarArchiveEntry("d/"), "d/");
    TarArchiveEntry link = new TarArchiveEntry("l", TarArchiveEntry.LF_SYMLINK);
    link.setLinkName("a.txt");
    TarBasicFileAttributes sym = TarBasicFileAttributes.fromEntry(link, "l");
    TarBasicFileAttributes implied = TarBasicFileAttributes.impliedDirectory("x/");

    assertTrue(file.isRegularFile());
    assertTrue(dir.isDirectory());
    assertTrue(sym.isSymbolicLink());
    assertTrue(implied.isDirectory());

    assertFalse(file.isOther());
    assertFalse(dir.isOther());
    assertFalse(sym.isOther());
    assertFalse(implied.isOther());
  }
}
