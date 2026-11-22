package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate32Test {

  @Test
  void entryNameJoinsSegmentsWithSlashWithoutLeadingSlash() {
    FileSystem fs = FileSystems.getDefault();

    SingleEntryArchivePath multi =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b/c");
    assertEquals("a/b/c", multi.entryName());

    SingleEntryArchivePath single =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a");
    assertEquals("a", single.entryName());

    SingleEntryArchivePath empty =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "");
    assertEquals("", empty.entryName());

    SingleEntryArchivePath absoluteRoot =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "");
    assertEquals("", absoluteRoot.entryName());

    SingleEntryArchivePath absolute =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.BZIP2, true, "x/y");
    assertEquals("x/y", absolute.entryName());
    assertEquals("/x/y", absolute.toString());

    SingleEntryArchivePath parent = (SingleEntryArchivePath) multi.getParent();
    assertEquals("a/b", parent.entryName());

    SingleEntryArchivePath sub = (SingleEntryArchivePath) multi.subpath(1, 3);
    assertEquals("b/c", sub.entryName());
  }
}
