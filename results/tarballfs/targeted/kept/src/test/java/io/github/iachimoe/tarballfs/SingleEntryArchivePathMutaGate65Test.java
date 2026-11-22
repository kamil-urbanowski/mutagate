package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate65Test {

  @Test
  void resolveRelativePathFromSameFileSystemAndFormatAppendsEntryName() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchivePath base =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a");
    SingleEntryArchivePath other =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "b/c");

    Path result = base.resolve(other);

    assertEquals("a/b/c", result.toString());
    assertEquals(
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, false, "a/b/c"), result);
  }
}
