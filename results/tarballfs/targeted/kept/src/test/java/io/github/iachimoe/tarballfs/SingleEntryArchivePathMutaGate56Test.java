package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate56Test {

  @Test
  void relativizeSkipsCommonLeadingSegments() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    SingleEntryArchivePath base = new SingleEntryArchivePath(fs, format, false, "a/b/c");
    SingleEntryArchivePath sibling = new SingleEntryArchivePath(fs, format, false, "a/b/d");
    SingleEntryArchivePath deeper = new SingleEntryArchivePath(fs, format, false, "a/b/c/d/e");
    SingleEntryArchivePath prefix = new SingleEntryArchivePath(fs, format, false, "a/b");

    Path siblingRel = base.relativize(sibling);
    assertEquals("../d", siblingRel.toString());
    assertEquals(2, siblingRel.getNameCount());

    assertEquals("d/e", base.relativize(deeper).toString());
    assertEquals("..", base.relativize(prefix).toString());

    SingleEntryArchivePath absBase = new SingleEntryArchivePath(fs, format, true, "x/y");
    SingleEntryArchivePath absOther = new SingleEntryArchivePath(fs, format, true, "x/z");
    assertEquals("../z", absBase.relativize(absOther).toString());
  }
}
