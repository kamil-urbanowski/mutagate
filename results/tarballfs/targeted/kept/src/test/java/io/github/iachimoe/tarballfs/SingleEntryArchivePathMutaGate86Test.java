package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate86Test {

  @Test
  void startsWithReturnsFalseForPathOfForeignType() {
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(FileSystems.getDefault(), SingleEntryArchiveFormat.GZIP, false, "a/b");

    Path foreign = Path.of("a");

    assertFalse(path.startsWith(foreign));
  }
}
