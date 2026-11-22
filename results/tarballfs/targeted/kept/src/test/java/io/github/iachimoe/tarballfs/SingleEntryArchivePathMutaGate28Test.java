package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate28Test {

  @Test
  void formatReturnsTheFormatGivenToTheConstructor() {
    FileSystem fs = FileSystems.getDefault();

    SingleEntryArchivePath gzipPath =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.GZIP, true, "entry.txt");
    SingleEntryArchivePath bzip2Path =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.BZIP2, false, "entry.txt");

    assertSame(SingleEntryArchiveFormat.GZIP, gzipPath.format());
    assertSame(SingleEntryArchiveFormat.BZIP2, bzip2Path.format());
  }
}
