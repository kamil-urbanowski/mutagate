package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate93Test {

  @Test
  void subpathAcceptsValidRangesAndRejectsInvalidOnes() {
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(FileSystems.getDefault(), SingleEntryArchiveFormat.GZIP, false, "a/b/c");

    Path full = path.subpath(0, 3);
    assertEquals("a/b/c", full.toString());
    assertEquals("b", path.subpath(1, 2).toString());
    assertEquals("a", path.subpath(0, 1).toString());
    assertEquals("b/c", path.subpath(1, 3).toString());

    assertThrows(IllegalArgumentException.class, () -> path.subpath(-1, 2));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(0, 4));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(2, 2));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(2, 1));
  }
}
