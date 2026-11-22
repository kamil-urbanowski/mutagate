package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate2Test {

  @Test
  void endsWithStringMatchesTrailingSegments() {
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(
            FileSystems.getDefault(), SingleEntryArchiveFormat.GZIP, false, "dir/sub/file.txt");

    assertTrue(path.endsWith("file.txt"));
    assertTrue(path.endsWith("sub/file.txt"));
    assertFalse(path.endsWith("other.txt"));
    assertFalse(path.endsWith("dir"));
  }
}
