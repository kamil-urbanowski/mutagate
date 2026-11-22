package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate41Test {

  @Test
  void isAbsoluteReflectsConstructorFlag() {
    FileSystem fs = FileSystems.getDefault();

    SingleEntryArchivePath relative =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "entry");
    SingleEntryArchivePath absolute =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "entry");

    assertFalse(relative.isAbsolute());
    assertTrue(absolute.isAbsolute());
  }
}
