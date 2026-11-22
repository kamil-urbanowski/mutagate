package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate76Test {

  @Test
  void startsWithPathOfEqualLengthIsTrue() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b");
    SingleEntryArchivePath same =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b");

    assertTrue(path.startsWith(same));
  }
}
