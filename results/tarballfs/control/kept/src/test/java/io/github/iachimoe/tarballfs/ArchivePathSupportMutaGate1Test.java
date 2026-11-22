package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ArchivePathSupportMutaGate1Test {

  @Test
  void parseHandlesAbsoluteRelativeNullAndEmptyParts() {
    ArchivePathSupport.ParsedUnixPath p1 = ArchivePathSupport.parse("/a", "b", "c");
    assertTrue(p1.absolute());
    assertEquals("a/b/c", p1.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath p2 = ArchivePathSupport.parse("a", "b");
    assertFalse(p2.absolute());
    assertEquals("a/b", p2.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath p3 = ArchivePathSupport.parse("/");
    assertTrue(p3.absolute());
    assertEquals("", p3.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath p4 = ArchivePathSupport.parse("/", "x");
    assertTrue(p4.absolute());
    assertEquals("x", p4.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath p5 = ArchivePathSupport.parse(null, "x", null, "", "y");
    assertFalse(p5.absolute());
    assertEquals("x/y", p5.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath p6 = ArchivePathSupport.parse("", "x");
    assertFalse(p6.absolute());
    assertEquals("x", p6.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath p7 = ArchivePathSupport.parse("a", (String[]) null);
    assertFalse(p7.absolute());
    assertEquals("a", p7.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath p8 = ArchivePathSupport.parse("a/", "b/");
    assertEquals("a//b/", p8.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath p9 = ArchivePathSupport.parse("//a");
    assertTrue(p9.absolute());
    assertEquals("/a", p9.relativeEntryKey());

    ArchivePathSupport.ParsedUnixPath p10 = ArchivePathSupport.parse("");
    assertFalse(p10.absolute());
    assertEquals("", p10.relativeEntryKey());
  }
}
