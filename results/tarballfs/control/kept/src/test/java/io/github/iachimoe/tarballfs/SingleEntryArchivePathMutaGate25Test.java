package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate25Test {

  @Test
  void getNameCountReflectsNumberOfSegmentsRegardlessOfAbsoluteFlag() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    assertEquals(0, new SingleEntryArchivePath(fs, format, false, "").getNameCount());
    assertEquals(0, new SingleEntryArchivePath(fs, format, true, "").getNameCount());

    assertEquals(1, new SingleEntryArchivePath(fs, format, false, "a").getNameCount());
    assertEquals(3, new SingleEntryArchivePath(fs, format, false, "a/b/c").getNameCount());
    assertEquals(2, new SingleEntryArchivePath(fs, format, true, "a/b").getNameCount());

    SingleEntryArchivePath path = new SingleEntryArchivePath(fs, format, false, "x/y/z");
    assertEquals(2, path.getParent().getNameCount());
    assertEquals(1, path.getFileName().getNameCount());
    assertEquals(2, path.subpath(1, 3).getNameCount());
    assertEquals(1, path.getName(0).getNameCount());
  }
}
