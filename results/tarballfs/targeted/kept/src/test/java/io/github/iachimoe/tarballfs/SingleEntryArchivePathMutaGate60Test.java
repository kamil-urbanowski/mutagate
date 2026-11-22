package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate60Test {

  @Test
  void relativizeAppendsRemainingSegmentsOfOtherPath() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchivePath base = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a");
    SingleEntryArchivePath other = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b/c");

    Path result = base.relativize(other);

    assertEquals("b/c", result.toString());
    assertEquals(2, result.getNameCount());
  }
}
