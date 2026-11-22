package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate55Test {

  @TempDir
  Path tempDir;

  @Test
  void relativizeSkipsCommonPrefixSegments() throws Exception {
    Path tar = tempDir.resolve("test.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath base = new TarPath(fs, false, "a/b/c");
    TarPath other = new TarPath(fs, false, "a/b/d/e");

    Path rel = base.relativize(other);
    assertEquals("../d/e", rel.toString());
    assertEquals(new TarPath(fs, false, "../d/e"), rel);

    TarPath same = new TarPath(fs, false, "x/y");
    assertEquals("", same.relativize(new TarPath(fs, false, "x/y")).toString());
  }
}
