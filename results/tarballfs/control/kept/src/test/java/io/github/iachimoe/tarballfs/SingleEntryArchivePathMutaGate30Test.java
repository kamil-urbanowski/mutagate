package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate30Test {

  @Test
  void compareToUsesStringRepresentationOfAnyPath() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    SingleEntryArchivePath relA = new SingleEntryArchivePath(fs, format, false, "a");
    SingleEntryArchivePath relB = new SingleEntryArchivePath(fs, format, false, "b");
    SingleEntryArchivePath relAB = new SingleEntryArchivePath(fs, format, false, "a/b");
    SingleEntryArchivePath absA = new SingleEntryArchivePath(fs, format, true, "a");
    SingleEntryArchivePath absRoot = new SingleEntryArchivePath(fs, format, true, "");

    // Lexicographic ordering and antisymmetry
    assertTrue(relA.compareTo(relB) < 0);
    assertTrue(relB.compareTo(relA) > 0);

    // A prefix sorts before the longer string
    assertTrue(relA.compareTo(relAB) < 0);
    assertTrue(relAB.compareTo(relA) > 0);

    // Absolute form has a leading '/', which sorts before letters
    assertTrue(absA.compareTo(relA) < 0);
    assertTrue(relA.compareTo(absA) > 0);
    assertTrue(absRoot.compareTo(relA) < 0);

    // Same value compares as zero
    assertEquals(0, relA.compareTo(relA));
    assertEquals(0, relAB.compareTo(new SingleEntryArchivePath(fs, format, false, "a/b")));

    // Comparison is based only on toString, so a foreign Path with the same text compares as 0
    Path foreignA = Path.of("a");
    assertFalse(relA.equals(foreignA));
    assertEquals(0, relA.compareTo(foreignA));
    assertTrue(relA.compareTo(Path.of("b")) < 0);
    assertTrue(relB.compareTo(Path.of("a")) > 0);

    // Different format with identical text also compares as 0
    SingleEntryArchivePath bzipA =
        new SingleEntryArchivePath(fs, SingleEntryArchiveFormat.BZIP2, false, "a");
    assertFalse(relA.equals(bzipA));
    assertEquals(0, relA.compareTo(bzipA));
  }
}
