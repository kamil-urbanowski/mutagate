package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate18Test {

  @TempDir
  Path tempDir;

  @Test
  void endsWithReturnsFalseWhenTrailingSegmentsDiffer() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[10240]);

    TarFileSystem fs = new TarFileSystem(
        new TarFileSystemProvider(),
        URI.create("tar:" + tar.toAbsolutePath() + "!"),
        tar);

    TarPath path = new TarPath(fs, false, "a/b/c");

    assertTrue(path.endsWith(new TarPath(fs, false, "b/c")));
    assertFalse(path.endsWith(new TarPath(fs, false, "a/c")));
    assertFalse(path.endsWith(new TarPath(fs, false, "x")));
  }
}
