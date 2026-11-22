package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate46Test {

  private static String normalized(boolean absolute, String entry) {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchivePath p =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, absolute, entry);
    return p.normalize().toString();
  }

  @Test
  void normalizeHandlesParentSegments() {
    // Normal segment followed by ".." is removed
    assertEquals("b", normalized(false, "a/../b"));
    // Leading ".." in a relative path is preserved
    assertEquals("../..", normalized(false, "../.."));
    // Excess ".." after popping is preserved in a relative path
    assertEquals("..", normalized(false, "a/b/../../.."));
    // Absolute path: ".." at root is dropped
    assertEquals("/", normalized(true, ".."));
    assertEquals("/b", normalized(true, "a/../../b"));
  }
}
