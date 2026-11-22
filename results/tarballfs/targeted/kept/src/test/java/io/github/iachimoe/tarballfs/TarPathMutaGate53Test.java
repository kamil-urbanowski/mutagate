package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate53Test {

  @TempDir
  Path tempDir;

  @Test
  void relativizeComputesCommonPrefixCorrectly() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);
    try {
      TarPath a = new TarPath(fs, false, "a/b/c");
      TarPath b = new TarPath(fs, false, "a/b/d");
      assertEquals("../d", a.relativize(b).toString());

      TarPath prefix = new TarPath(fs, false, "a/b");
      TarPath longer = new TarPath(fs, false, "a/b/c/d");
      assertEquals("c/d", prefix.relativize(longer).toString());
      assertEquals("../..", longer.relativize(prefix).toString());
    } finally {
      fs.close();
    }
  }
}
