package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate15Test {

  @Test
  void endsWithReturnsFalseForForeignPathTypeAndTrueForMatchingArchivePath() {
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(FileSystems.getDefault(), SingleEntryArchiveFormat.GZIP, false, "a");

    Path foreign = Path.of("a");
    assertFalse(path.endsWith(foreign));

    SingleEntryArchivePath same =
        new SingleEntryArchivePath(FileSystems.getDefault(), SingleEntryArchiveFormat.GZIP, false, "a");
    assertTrue(path.endsWith(same));
  }
}
