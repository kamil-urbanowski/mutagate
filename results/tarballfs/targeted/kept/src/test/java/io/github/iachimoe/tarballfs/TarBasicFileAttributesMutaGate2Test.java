package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate2Test {

  @Test
  void fileKeyReturnsTheEntryKeyGiven() {
    String key = "some/entry/key.txt";
    TarBasicFileAttributes fromEntry =
        TarBasicFileAttributes.fromEntry(new TarArchiveEntry("some/entry/key.txt"), key);
    assertSame(key, fromEntry.fileKey());

    TarBasicFileAttributes implied = TarBasicFileAttributes.impliedDirectory("dir/");
    assertEquals("dir/", implied.fileKey());
  }
}
