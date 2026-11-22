package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate15Test {

  @Test
  void endsWithReturnsTrueWhenSuffixSegmentsMatch(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("test.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);

    TarPath path = new TarPath(fs, false, "a/b/c");

    assertTrue(path.endsWith(new TarPath(fs, false, "b/c")));
    assertTrue(path.endsWith(new TarPath(fs, false, "a/b/c")));
    assertFalse(path.endsWith(new TarPath(fs, false, "a/c")));
  }
}
