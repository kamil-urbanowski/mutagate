package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate25Test {

  @Test
  void pathIsEqualToItself() {
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(
            FileSystems.getDefault(), SingleEntryArchiveFormat.GZIP, true, "entry.txt");

    assertTrue(path.equals(path));
  }
}
