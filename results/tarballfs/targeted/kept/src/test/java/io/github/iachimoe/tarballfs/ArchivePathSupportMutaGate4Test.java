package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ArchivePathSupportMutaGate4Test {

  @Test
  void parseKeepsNonEmptyFirstPart() {
    ArchivePathSupport.ParsedUnixPath relative = ArchivePathSupport.parse("a", "b");
    assertFalse(relative.absolute());
    assertEquals("a/b", relative.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath single = ArchivePathSupport.parse("dir");
    assertEquals("dir", single.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath absolute = ArchivePathSupport.parse("/x", "y");
    assertTrue(absolute.absolute());
    assertEquals("x/y", absolute.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath root = ArchivePathSupport.parse("/", "y");
    assertTrue(root.absolute());
    assertEquals("y", root.relativeEntryKey());
  }
}
