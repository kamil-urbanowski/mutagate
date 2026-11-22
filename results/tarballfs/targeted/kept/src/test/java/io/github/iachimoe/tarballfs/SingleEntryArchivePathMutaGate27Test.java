package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate27Test {

  @Test
  void equalsReturnsFalseWhenStateDiffers() {
    FileSystem fs = FileSystems.getDefault();

    SingleEntryArchivePath base = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b");
    SingleEntryArchivePath same = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b");
    SingleEntryArchivePath otherSegments =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/c");
    SingleEntryArchivePath otherAbsolute =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "a/b");
    SingleEntryArchivePath otherFormat =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.BZIP2, false, "a/b");

    assertEquals(base, same);
    assertFalse(base.equals(otherSegments));
    assertFalse(base.equals(otherAbsolute));
    assertFalse(base.equals(otherFormat));
  }
}
