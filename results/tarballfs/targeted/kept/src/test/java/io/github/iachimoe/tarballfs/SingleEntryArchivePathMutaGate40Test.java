package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate40Test {

  @Test
  void isAbsoluteReflectsConstructorFlag() {
    FileSystem fs = FileSystems.getDefault();

    SingleEntryArchivePath absolutePath =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "entry.txt");
    SingleEntryArchivePath relativePath =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "entry.txt");

    assertTrue(absolutePath.isAbsolute());
    assertFalse(relativePath.isAbsolute());
  }
}
