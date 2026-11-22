package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate57Test {

  @Test
  void relativizeSkipsCommonPrefixSegments() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchivePath base =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b");
    SingleEntryArchivePath other =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/c");

    Path result = base.relativize(other);

    assertEquals("../c", result.toString());
  }
}
