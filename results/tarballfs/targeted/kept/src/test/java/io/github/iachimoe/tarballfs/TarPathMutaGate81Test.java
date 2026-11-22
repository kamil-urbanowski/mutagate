package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate81Test {

  @TempDir
  Path tempDir;

  @Test
  void startsWithReturnsFalseWhenAbsoluteFlagsDiffer() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(
        new TarFileSystemProvider(),
        java.net.URI.create("tar:" + tar.toAbsolutePath() + "!"),
        tar);

    TarPath absolute = new TarPath(fs, true, "a/b");
    TarPath relative = new TarPath(fs, false, "a");
    TarPath absolutePrefix = new TarPath(fs, true, "a");

    assertFalse(absolute.startsWith(relative));
    assertFalse(relative.startsWith(absolutePrefix));
    assertTrue(absolute.startsWith(absolutePrefix));
  }
}
