package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SingleEntryArchivePathMutaGate1Test {

  private static SingleEntryArchivePath p(
      FileSystem fs, SingleEntryArchiveFormat format, boolean absolute, String name) {
    return new SingleEntryArchivePath(fs, format, absolute, name);
  }

  @Test
  void relativizeBehaviors() {
    FileSystem fs = FileSystems.getDefault();
    SingleEntryArchiveFormat gz = SingleEntryArchiveFormat.GZIP;

    // descendant
    Path down = p(fs, gz, false, "a/b").relativize(p(fs, gz, false, "a/b/c/d"));
    assertEquals("c/d", down.toString());
    assertFalse(down.isAbsolute());

    // diverging branches need ".." for each remaining segment of this
    assertEquals("../../x", p(fs, gz, false, "a/b/c").relativize(p(fs, gz, false, "a/x")).toString());

    // ancestor
    assertEquals("../..", p(fs, gz, false, "a/b/c").relativize(p(fs, gz, false, "a")).toString());

    // identical paths
    assertEquals("", p(fs, gz, false, "a/b").relativize(p(fs, gz, false, "a/b")).toString());

    // empty this
    assertEquals("a/b", p(fs, gz, false, "").relativize(p(fs, gz, false, "a/b")).toString());

    // no common prefix
    assertEquals("../y/z", p(fs, gz, false, "x").relativize(p(fs, gz, false, "y/z")).toString());

    // absolute paths yield a relative result
    Path abs = p(fs, gz, true, "a/b").relativize(p(fs, gz, true, "a/c"));
    assertEquals("../c", abs.toString());
    assertFalse(abs.isAbsolute());

    // errors
    assertThrows(NullPointerException.class, () -> p(fs, gz, false, "a").relativize(null));
    assertThrows(
        IllegalArgumentException.class,
        () -> p(fs, gz, false, "a").relativize(p(fs, gz, true, "a")));
    assertThrows(
        IllegalArgumentException.class,
        () -> p(fs, gz, true, "a").relativize(p(fs, gz, false, "a")));
    assertThrows(
        IllegalArgumentException.class,
        () -> p(fs, gz, false, "a").relativize(p(fs, SingleEntryArchiveFormat.BZIP2, false, "a")));
    assertThrows(
        IllegalArgumentException.class, () -> p(fs, gz, false, "a").relativize(Path.of("a")));
  }
}
