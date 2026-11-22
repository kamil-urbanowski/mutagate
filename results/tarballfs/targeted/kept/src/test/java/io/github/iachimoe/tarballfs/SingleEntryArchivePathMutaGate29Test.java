package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate29Test {

  @Test
  void getNameReturnsRelativeSingleSegmentPath() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchivePath path =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "a/b/c");

    Path name = path.getName(1);

    assertNotNull(name);
    assertEquals("b", name.toString());
    assertFalse(name.isAbsolute());
    assertEquals(1, name.getNameCount());
    assertEquals(new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "b"), name);
  }
}
