package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ArchivePathSupportMutaGate2Test {

  @Test
  void noLeadingSeparatorWhenFirstPartIsEmpty() {
    ArchivePathSupport.ParsedUnixPath emptyFirst = ArchivePathSupport.parse("", "a", "b");
    assertEquals("a/b", emptyFirst.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath rootFirst = ArchivePathSupport.parse("/", "a");
    assertTrue(rootFirst.absolute());
    assertEquals("a", rootFirst.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath nonEmptyFirst = ArchivePathSupport.parse("x", "y");
    assertEquals("x/y", nonEmptyFirst.relativeEntryKey());
  }
}
