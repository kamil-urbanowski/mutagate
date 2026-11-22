package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate78Test {

  @Test
  void startsWithAcceptsSameKindOfPathAndRejectsForeignPath() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchivePath full = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b");
    SingleEntryArchivePath prefix = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a");
    SingleEntryArchivePath other = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "b");
    Path foreign = fs.getPath("a");

    assertTrue(full.startsWith(prefix));
    assertFalse(full.startsWith(other));
    assertFalse(full.startsWith(foreign));
  }
}
