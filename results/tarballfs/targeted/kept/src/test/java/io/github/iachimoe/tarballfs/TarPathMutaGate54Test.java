package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate54Test {

  @Test
  void relativizeSkipsCommonPrefixSegments() throws Exception {
    Path tar = Files.createTempFile("mutagate54", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

      TarPath base = new TarPath(fs, false, "a/b/c");
      TarPath sibling = new TarPath(fs, false, "a/b/d");
      TarPath deeper = new TarPath(fs, false, "a/b/c/d/e");
      TarPath shorter = new TarPath(fs, false, "a");

      assertEquals("../d", base.relativize(sibling).toString());
      assertEquals("d/e", base.relativize(deeper).toString());
      assertEquals("b/c", shorter.relativize(base).toString());
      assertEquals("../..", base.relativize(shorter).toString());
      assertEquals("", base.relativize(base).toString());
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
