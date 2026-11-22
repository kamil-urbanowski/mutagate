package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate17Test {

  @Test
  void constructorValidatesArgumentsAndStoresState() {
    FileSystem fs = FileSystems.getDefault();

    NullPointerException fsEx = assertThrows(
        NullPointerException.class,
        () -> new SingleEntryArchivePath(null, SingleEntryArchiveFormat.GZIP, false, "a"));
    assertEquals("fileSystem", fsEx.getMessage());

    NullPointerException formatEx = assertThrows(
        NullPointerException.class,
        () -> new SingleEntryArchivePath(fs, null, false, "a"));
    assertEquals("format", formatEx.getMessage());

    SingleEntryArchivePath rel = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.BZIP2, false, "a/b");
    assertSame(fs, rel.getFileSystem());
    assertSame(SingleEntryArchiveFormat.BZIP2, rel.format());
    assertFalse(rel.isAbsolute());
    assertEquals(2, rel.getNameCount());
    assertEquals("a/b", rel.entryName());
    assertEquals("a/b", rel.toString());

    SingleEntryArchivePath abs = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "a/b");
    assertSame(SingleEntryArchiveFormat.GZIP, abs.format());
    assertTrue(abs.isAbsolute());
    assertEquals("/a/b", abs.toString());

    SingleEntryArchivePath emptyRel = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "");
    assertEquals(0, emptyRel.getNameCount());
    assertEquals("", emptyRel.toString());

    SingleEntryArchivePath emptyAbs = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "");
    assertEquals(0, emptyAbs.getNameCount());
    assertEquals("/", emptyAbs.toString());
  }
}
