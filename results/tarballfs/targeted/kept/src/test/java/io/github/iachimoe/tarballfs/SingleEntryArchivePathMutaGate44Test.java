package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate44Test {

  @Test
  void normalizeRemovesPrecedingSegmentForDotDot() {
    SingleEntryArchivePath path = new SingleEntryArchivePath(
        FileSystems.getDefault(), SingleEntryArchiveFormat.GZIP, false, "a/b/../c");

    Path normalized = path.normalize();

    assertEquals("a/c", normalized.toString());
    assertEquals(2, normalized.getNameCount());
  }
}
