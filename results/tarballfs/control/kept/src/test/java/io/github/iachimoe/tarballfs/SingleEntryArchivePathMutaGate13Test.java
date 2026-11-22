package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate13Test {

  @Test
  void subpathReturnsRelativePathForRangeAndRejectsInvalidRanges() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchivePath abs =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "a/b/c/d");

    Path middle = abs.subpath(1, 3);
    assertEquals("b/c", middle.toString());
    assertFalse(middle.isAbsolute());
    assertEquals(2, middle.getNameCount());
    assertSame(fs, middle.getFileSystem());
    assertEquals(
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "b/c"), middle);
    assertEquals(SingleEntryArchiveFormat.GZIP, ((SingleEntryArchivePath) middle).format());

    // single element at start and at end (end == length is allowed)
    assertEquals("a", abs.subpath(0, 1).toString());
    assertEquals("d", abs.subpath(3, 4).toString());

    // full range of an absolute path yields a relative path
    Path full = abs.subpath(0, 4);
    assertEquals("a/b/c/d", full.toString());
    assertFalse(full.isAbsolute());

    // invalid ranges
    assertThrows(IllegalArgumentException.class, () -> abs.subpath(-1, 2));
    assertThrows(IllegalArgumentException.class, () -> abs.subpath(0, 5));
    assertThrows(IllegalArgumentException.class, () -> abs.subpath(2, 2));
    assertThrows(IllegalArgumentException.class, () -> abs.subpath(3, 1));
    assertThrows(IllegalArgumentException.class, () -> abs.subpath(4, 5));
  }
}
