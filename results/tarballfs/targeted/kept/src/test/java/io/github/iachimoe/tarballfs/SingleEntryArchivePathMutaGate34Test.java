package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate34Test {

  @Test
  void getParentOfSingleSegmentPathDependsOnAbsolute() {
    FileSystem fs = FileSystems.getDefault();

    SingleEntryArchivePath relative =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "foo");
    assertNull(relative.getParent());

    SingleEntryArchivePath absolute =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "foo");
    Path parent = absolute.getParent();
    assertNotNull(parent);
    assertTrue(parent.isAbsolute());
    assertEquals(0, parent.getNameCount());
    assertEquals("/", parent.toString());
  }
}
