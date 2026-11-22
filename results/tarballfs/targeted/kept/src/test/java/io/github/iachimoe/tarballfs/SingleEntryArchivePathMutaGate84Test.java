package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate84Test {

  @Test
  void startsWithComparesSegmentsOneByOne() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b/c");
    SingleEntryArchivePath matchingPrefix =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b");
    SingleEntryArchivePath differingPrefix =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/x");

    assertTrue(path.startsWith(matchingPrefix));
    assertFalse(path.startsWith(differingPrefix));
  }
}
