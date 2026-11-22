package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate48Test {

  @Test
  void relativizeStopsAtEndOfEitherPath() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    SingleEntryArchivePath shorter = new SingleEntryArchivePath(fs, format, false, "a/b");
    SingleEntryArchivePath longer = new SingleEntryArchivePath(fs, format, false, "a/b/c");

    // The common prefix equals this path's full length
    assertEquals("c", shorter.relativize(longer).toString());
    // The common prefix equals the other path's full length
    assertEquals("..", longer.relativize(shorter).toString());
  }
}
