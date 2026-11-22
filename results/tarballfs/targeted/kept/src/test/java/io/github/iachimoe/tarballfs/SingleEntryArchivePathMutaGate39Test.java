package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate39Test {

  private static int expectedHash(FileSystem fs, SingleEntryArchiveFormat format, boolean absolute, String entry) {
    int result = Objects.hash(format, System.identityHashCode(fs), absolute);
    return 31 * result + Arrays.hashCode(ArchivePathSupport.pathSegments(entry));
  }

  @Test
  void hashCodeMatchesFormulaAndIsNotConstantZero() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;

    SingleEntryArchivePath p1 = new SingleEntryArchivePath(fs, format, true, "a/b");
    SingleEntryArchivePath p2 = new SingleEntryArchivePath(fs, format, false, "c");
    SingleEntryArchivePath p1Copy = new SingleEntryArchivePath(fs, format, true, "a/b");

    assertEquals(expectedHash(fs, format, true, "a/b"), p1.hashCode());
    assertEquals(expectedHash(fs, format, false, "c"), p2.hashCode());
    assertEquals(p1.hashCode(), p1Copy.hashCode());
  }
}
