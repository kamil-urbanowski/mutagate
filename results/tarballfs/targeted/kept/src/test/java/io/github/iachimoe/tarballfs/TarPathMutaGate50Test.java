package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate50Test {

  @TempDir
  Path tempDir;

  @Test
  void relativizeAppendsRemainingSegmentsOfOther() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystem fs = new TarFileSystem(
        new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath base = new TarPath(fs, false, "a/b");
    TarPath other = new TarPath(fs, false, "a/c/d");
    assertEquals("../c/d", base.relativize(other).toString());

    TarPath child = new TarPath(fs, false, "a/b/c");
    assertEquals("c", base.relativize(child).toString());
  }
}
