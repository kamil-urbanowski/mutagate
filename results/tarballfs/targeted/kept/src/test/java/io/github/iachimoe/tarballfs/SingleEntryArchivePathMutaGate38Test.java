package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate38Test {

  @Test
  void hashCodeCombinesBaseHashAndSegmentsHashWithAddition() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    SingleEntryArchivePath path = new SingleEntryArchivePath(fs, format, false, "a");

    int base = Objects.hash(format, System.identityHashCode(fs), false);
    int segmentsHash = java.util.Arrays.hashCode(new String[] {"a"});
    int expected = 31 * base + segmentsHash;

    assertEquals(expected, path.hashCode());
  }
}
