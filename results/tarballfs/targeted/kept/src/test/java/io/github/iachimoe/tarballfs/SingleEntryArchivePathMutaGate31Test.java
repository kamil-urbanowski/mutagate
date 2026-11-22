package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate31Test {

  @Test
  void getParentDropsOnlyTheLastSegment() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchivePath relative =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b/c");
    Path parent = relative.getParent();

    assertEquals("a/b", parent.toString());
    assertEquals(2, parent.getNameCount());
    assertEquals(
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b"), parent);

    SingleEntryArchivePath absolute =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "x/y");
    assertEquals("/x", absolute.getParent().toString());
  }
}
