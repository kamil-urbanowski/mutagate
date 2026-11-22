package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate95Test {

  @Test
  void subpathValidatesRange() {
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(FileSystems.getDefault(), SingleEntryArchiveFormat.GZIP, false, "a/b/c");

    Path valid = path.subpath(1, 3);
    assertEquals("b/c", valid.toString());
    assertFalse(valid.isAbsolute());
    assertEquals("a/b/c", path.subpath(0, 3).toString());
    assertEquals("a", path.subpath(0, 1).toString());

    assertThrows(IllegalArgumentException.class, () -> path.subpath(-1, 2));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(0, 4));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(2, 2));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(2, 1));
  }
}
