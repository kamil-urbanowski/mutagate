package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate39Test {

  @Test
  void isAbsoluteReflectsConstructorFlag(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("archive.tar");
    Files.write(tar, new byte[1024]);

    URI uri = URI.create("tar:" + tar.toAbsolutePath() + "!");
    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), uri, tar);
    try {
      TarPath relative = new TarPath(fs, false, "a/b");
      TarPath absolute = new TarPath(fs, true, "a/b");

      assertFalse(relative.isAbsolute());
      assertTrue(absolute.isAbsolute());
      assertFalse(relative.getParent().isAbsolute());
      assertTrue(absolute.toAbsolutePath().isAbsolute());
    } finally {
      fs.close();
    }
  }
}
