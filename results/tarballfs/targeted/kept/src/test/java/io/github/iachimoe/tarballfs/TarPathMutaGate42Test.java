package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate42Test {

  @Test
  void normalizeRemovesPrecedingSegmentForDotDot() throws Exception {
    Path dir = Files.createTempDirectory("tarpath-test");
    Path tar = dir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);
    try {
      TarFileSystem fs = new TarFileSystem(
          new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

      TarPath relative = new TarPath(fs, false, "a/b/../c");
      assertEquals(new TarPath(fs, false, "a/c"), relative.normalize());
      assertEquals("a/c", relative.normalize().toString());

      TarPath absolute = new TarPath(fs, true, "x/y/z/../../w");
      assertEquals("/x/w", absolute.normalize().toString());
    } finally {
      Files.deleteIfExists(tar);
      Files.deleteIfExists(dir);
    }
  }
}
