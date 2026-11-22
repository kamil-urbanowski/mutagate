package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate38Test {

  @Test
  void isAbsoluteReflectsConstructorFlag(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("test.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(
        new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath absolute = new TarPath(fs, true, "a/b");
    TarPath relative = new TarPath(fs, false, "a/b");

    assertTrue(absolute.isAbsolute());
    assertFalse(relative.isAbsolute());
    assertTrue(relative.toAbsolutePath().isAbsolute());
  }
}
