package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate15Test {

  @Test
  void getFileNameReturnsRelativeLastSegmentSameFileSystemAndFormat() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    // Absolute multi-segment path: file name is relative last segment only
    SingleEntryArchivePath abs = new SingleEntryArchivePath(fs, format, true, "a/b/c.txt");
    Path name = abs.getFileName();
    assertNotNull(name);
    assertFalse(name.isAbsolute());
    assertEquals("c.txt", name.toString());
    assertEquals(1, name.getNameCount());
    assertNull(name.getParent());
    assertNull(name.getRoot());
    assertSame(fs, name.getFileSystem());
    assertEquals(new SingleEntryArchivePath(fs, format, false, "c.txt"), name);
    assertEquals(format, SingleEntryArchivePath.require(name, format).format());

    // Absolute single-segment path: file name drops the absolute flag
    SingleEntryArchivePath absSingle = new SingleEntryArchivePath(fs, format, true, "only");
    Path singleName = absSingle.getFileName();
    assertNotNull(singleName);
    assertFalse(singleName.isAbsolute());
    assertEquals("only", singleName.toString());

    // Relative multi-segment path
    SingleEntryArchivePath rel = new SingleEntryArchivePath(fs, format, false, "x/y");
    assertEquals("y", rel.getFileName().toString());

    // Different format is preserved
    SingleEntryArchivePath bz = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.BZIP2, false, "p/q");
    assertEquals(
        SingleEntryArchiveFormat.BZIP2,
        SingleEntryArchivePath.require(bz.getFileName(), SingleEntryArchiveFormat.BZIP2).format());

    // Paths with no segments have no file name
    assertNull(new SingleEntryArchivePath(fs, format, true, "").getFileName());
    assertNull(new SingleEntryArchivePath(fs, format, false, "").getFileName());
  }
}
