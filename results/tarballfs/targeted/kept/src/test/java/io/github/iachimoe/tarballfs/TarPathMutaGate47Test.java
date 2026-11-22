package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate47Test {

  @TempDir
  Path tempDir;

  @Test
  void relativizeHandlesPrefixPathsInBothDirections() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[10240]);
    TarFileSystem fs = new TarFileSystem(
        new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath shorter = new TarPath(fs, true, "a/b");
    TarPath longer = new TarPath(fs, true, "a/b/c/d");

    // this is a prefix of other: the loop must stop at this.segments.length
    assertEquals("c/d", shorter.relativize(longer).toString());
    // other is a prefix of this: the loop must stop at tp.segments.length
    assertEquals("../..", longer.relativize(shorter).toString());
  }
}
