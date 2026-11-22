package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate8Test {

  @Test
  void getParentBehavesForRelativeAndAbsolutePaths() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    // Multi-segment relative path: drops only the last segment, stays relative
    SingleEntryArchivePath rel = new SingleEntryArchivePath(fs, format, false, "a/b/c");
    Path relParent = rel.getParent();
    assertEquals(new SingleEntryArchivePath(fs, format, false, "a/b"), relParent);
    assertEquals("a/b", relParent.toString());
    assertEquals(2, relParent.getNameCount());
    assertTrue(!relParent.isAbsolute());
    assertSame(fs, relParent.getFileSystem());

    // Walk up the whole relative chain
    Path relParent2 = relParent.getParent();
    assertEquals("a", relParent2.toString());
    assertNull(relParent2.getParent());

    // Multi-segment absolute path: stays absolute
    SingleEntryArchivePath abs = new SingleEntryArchivePath(fs, format, true, "x/y/z");
    Path absParent = abs.getParent();
    assertEquals(new SingleEntryArchivePath(fs, format, true, "x/y"), absParent);
    assertEquals("/x/y", absParent.toString());
    assertTrue(absParent.isAbsolute());

    // Absolute single segment: parent is the root
    Path absSingleParent = absParent.getParent().getParent();
    assertEquals("/", absSingleParent.toString());
    assertTrue(absSingleParent.isAbsolute());
    assertEquals(0, absSingleParent.getNameCount());
    assertEquals(new SingleEntryArchivePath(fs, format, true, ""), absSingleParent);

    // Root has no parent
    assertNull(absSingleParent.getParent());

    // Relative single segment has no parent
    assertNull(new SingleEntryArchivePath(fs, format, false, "only").getParent());

    // Empty relative path has no parent
    assertNull(new SingleEntryArchivePath(fs, format, false, "").getParent());

    // Format is preserved across getParent
    SingleEntryArchivePath bz = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.BZIP2, false, "p/q");
    SingleEntryArchivePath bzParent = (SingleEntryArchivePath) bz.getParent();
    assertEquals(SingleEntryArchiveFormat.BZIP2, bzParent.format());
    assertEquals("p", bzParent.entryName());
  }
}
