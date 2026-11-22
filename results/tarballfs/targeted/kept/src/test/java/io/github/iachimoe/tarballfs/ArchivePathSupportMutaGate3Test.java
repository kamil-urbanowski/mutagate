package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ArchivePathSupportMutaGate3Test {

  @Test
  void parseStripsLeadingSlashOnlyForAbsolutePaths() {
    ArchivePathSupport.ParsedUnixPath absolute = ArchivePathSupport.parse("/a/b", "c");
    assertTrue(absolute.absolute());
    assertEquals("a/b/c", absolute.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath relative = ArchivePathSupport.parse("a/b", "c");
    assertFalse(relative.absolute());
    assertEquals("a/b/c", relative.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath empty = ArchivePathSupport.parse("");
    assertFalse(empty.absolute());
    assertEquals("", empty.relativeEntryKey());
  }
}
