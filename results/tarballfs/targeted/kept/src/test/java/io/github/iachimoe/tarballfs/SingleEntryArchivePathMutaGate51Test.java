package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate51Test {

  @Test
  void relativizeAppendsRemainingSegmentsOfOtherPath() {
    var fs = FileSystems.getDefault();
    SingleEntryArchivePath base =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/x");
    SingleEntryArchivePath other =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b/c");

    Path rel = base.relativize(other);

    assertEquals("../b/c", rel.toString());
    assertEquals(3, rel.getNameCount());
  }
}
