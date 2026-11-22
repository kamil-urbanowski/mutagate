package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate30Test {

  @Test
  void getNameCountReturnsNumberOfSegments() {
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(
            FileSystems.getDefault(), SingleEntryArchiveFormat.GZIP, false, "a/b");

    assertEquals(2, path.getNameCount());
  }
}
