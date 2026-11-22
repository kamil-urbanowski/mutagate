package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate73Test {

  @Test
  void resolveSiblingWithParentReturnsParentResolvedPath() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b");
    SingleEntryArchivePath other =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "c");

    Path result = path.resolveSibling(other);

    assertNotNull(result);
    assertEquals("a/c", result.toString());
  }
}
