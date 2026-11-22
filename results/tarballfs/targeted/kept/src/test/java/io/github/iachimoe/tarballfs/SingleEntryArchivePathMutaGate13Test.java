package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate13Test {

  @Test
  void endsWithComparesSegmentsFromTheEnd() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    SingleEntryArchivePath path = new SingleEntryArchivePath(fs, format, false, "a/b/c");
    SingleEntryArchivePath matchingSuffix = new SingleEntryArchivePath(fs, format, false, "b/c");
    SingleEntryArchivePath wrongSuffix = new SingleEntryArchivePath(fs, format, false, "x/c");
    SingleEntryArchivePath wrongLast = new SingleEntryArchivePath(fs, format, false, "b/d");

    assertTrue(path.endsWith(matchingSuffix));
    assertTrue(path.endsWith(path));
    assertFalse(path.endsWith(wrongSuffix));
    assertFalse(path.endsWith(wrongLast));
  }
}
