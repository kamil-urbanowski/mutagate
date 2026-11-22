package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate26Test {

  @Test
  void equalsReturnsFalseForObjectsOfOtherTypes() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchivePath path = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b");
    SingleEntryArchivePath same = new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b");

    assertTrue(path.equals(same));
    assertFalse(path.equals("a/b"));
    assertFalse(path.equals(null));
    assertFalse(path.equals(Path.of("a/b")));
    assertFalse(path.equals(new Object()));
    assertEquals(path.hashCode(), same.hashCode());
  }
}
