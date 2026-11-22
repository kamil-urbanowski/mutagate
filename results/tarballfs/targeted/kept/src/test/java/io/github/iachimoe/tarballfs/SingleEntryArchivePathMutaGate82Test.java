package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate82Test {

  @Test
  void startsWithHandlesPrefixLengthComparison() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    SingleEntryArchivePath longer = new SingleEntryArchivePath(fs, format, false, "a/b");
    SingleEntryArchivePath prefix = new SingleEntryArchivePath(fs, format, false, "a");
    SingleEntryArchivePath same = new SingleEntryArchivePath(fs, format, false, "a/b");
    SingleEntryArchivePath tooLong = new SingleEntryArchivePath(fs, format, false, "a/b/c");

    assertTrue(longer.startsWith(prefix));
    assertTrue(longer.startsWith(same));
    assertFalse(longer.startsWith(tooLong));
  }
}
