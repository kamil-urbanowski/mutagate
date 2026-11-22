package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate23Test {

  @Test
  void isAbsoluteReflectsConstructionFlagAndIsPropagatedByDerivedPaths() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    SingleEntryArchivePath abs = new SingleEntryArchivePath(fs, format, true, "a/b");
    SingleEntryArchivePath rel = new SingleEntryArchivePath(fs, format, false, "a/b");
    SingleEntryArchivePath absEmpty = new SingleEntryArchivePath(fs, format, true, "");
    SingleEntryArchivePath relEmpty = new SingleEntryArchivePath(fs, format, false, "");

    // Direct flag
    assertTrue(abs.isAbsolute());
    assertFalse(rel.isAbsolute());
    assertTrue(absEmpty.isAbsolute());
    assertFalse(relEmpty.isAbsolute());

    // Absolute conversion
    assertTrue(rel.toAbsolutePath().isAbsolute());
    assertTrue(abs.toAbsolutePath().isAbsolute());

    // Root
    Path root = abs.getRoot();
    assertNotNull(root);
    assertTrue(root.isAbsolute());
    assertNull(rel.getRoot());

    // Parent
    Path absParent = abs.getParent();
    assertNotNull(absParent);
    assertTrue(absParent.isAbsolute());
    Path relParent = rel.getParent();
    assertNotNull(relParent);
    assertFalse(relParent.isAbsolute());

    SingleEntryArchivePath absSingle = new SingleEntryArchivePath(fs, format, true, "a");
    Path singleParent = absSingle.getParent();
    assertNotNull(singleParent);
    assertTrue(singleParent.isAbsolute());
    SingleEntryArchivePath relSingle = new SingleEntryArchivePath(fs, format, false, "a");
    assertNull(relSingle.getParent());

    // Name-based derivations are always relative
    assertFalse(abs.getFileName().isAbsolute());
    assertFalse(abs.getName(0).isAbsolute());
    assertFalse(abs.subpath(0, 1).isAbsolute());

    // Normalize and resolve preserve the base's flag
    assertTrue(abs.normalize().isAbsolute());
    assertFalse(rel.normalize().isAbsolute());
    assertTrue(abs.resolve("c").isAbsolute());
    assertFalse(rel.resolve("c").isAbsolute());

    // Relativize yields a relative path
    SingleEntryArchivePath absOther = new SingleEntryArchivePath(fs, format, true, "a/c");
    assertFalse(abs.relativize(absOther).isAbsolute());

    // Absolute and relative paths with same segments are not equal
    assertFalse(abs.equals(rel));
  }
}
