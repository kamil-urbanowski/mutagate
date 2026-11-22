package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate5Test {

  @Test
  void isOtherIsFalseForAllKinds() {
    TarBasicFileAttributes file =
        TarBasicFileAttributes.fromEntry(new TarArchiveEntry("a.txt"), "a.txt");
    TarBasicFileAttributes dir =
        TarBasicFileAttributes.fromEntry(new TarArchiveEntry("d/"), "d/");
    TarBasicFileAttributes implied = TarBasicFileAttributes.impliedDirectory("x/");

    assertFalse(file.isOther());
    assertFalse(dir.isOther());
    assertFalse(implied.isOther());
  }
}
