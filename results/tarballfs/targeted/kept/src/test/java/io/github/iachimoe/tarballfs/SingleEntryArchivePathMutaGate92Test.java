package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate92Test {

  @Test
  void subpathBoundaries() {
    SingleEntryArchivePath path = new SingleEntryArchivePath(
        FileSystems.getDefault(), SingleEntryArchiveFormat.GZIP, false, "a/b/c");

    assertEquals(3, path.getNameCount());

    // beginIndex == 0 is valid
    Path first = path.subpath(0, 1);
    assertEquals("a", first.toString());

    // endIndex == segments.length is valid
    Path whole = path.subpath(0, 3);
    assertEquals("a/b/c", whole.toString());

    Path tail = path.subpath(1, 3);
    assertEquals("b/c", tail.toString());

    Path single = path.subpath(2, 3);
    assertEquals("c", single.toString());

    // beginIndex == endIndex is invalid
    assertThrows(IllegalArgumentException.class, () -> path.subpath(1, 1));
    assertThrows(IllegalArgumentException.class, () -> path.subpath(0, 0));

    // beginIndex > endIndex is invalid
    assertThrows(IllegalArgumentException.class, () -> path.subpath(2, 1));

    // negative begin is invalid
    assertThrows(IllegalArgumentException.class, () -> path.subpath(-1, 1));

    // end beyond length is invalid
    assertThrows(IllegalArgumentException.class, () -> path.subpath(0, 4));
  }
}
