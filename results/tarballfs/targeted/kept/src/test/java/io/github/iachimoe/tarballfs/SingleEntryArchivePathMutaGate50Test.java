package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate50Test {

  @Test
  void relativizeAddsOneParentSegmentPerRemainingSegmentOfThisPath() {
    var fs = FileSystems.getDefault();
    SingleEntryArchivePath base =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b");
    SingleEntryArchivePath target =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/c");

    Path result = base.relativize(target);

    assertEquals("../c", result.toString());
    assertEquals(2, result.getNameCount());
  }
}
