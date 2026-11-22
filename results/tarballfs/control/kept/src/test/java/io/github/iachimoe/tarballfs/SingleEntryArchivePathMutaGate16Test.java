package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate16Test {

  @Test
  void toStringReflectsAbsoluteFlagAndSegments() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    assertEquals("", new SingleEntryArchivePath(fs, format, false, "").toString());
    assertEquals("/", new SingleEntryArchivePath(fs, format, true, "").toString());
    assertEquals("a", new SingleEntryArchivePath(fs, format, false, "a").toString());
    assertEquals("/a", new SingleEntryArchivePath(fs, format, true, "a").toString());
    assertEquals("a/b", new SingleEntryArchivePath(fs, format, false, "a/b").toString());
    assertEquals("/a/b/c", new SingleEntryArchivePath(fs, format, true, "a/b/c").toString());
  }
}
