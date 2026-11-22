package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate46Test {

  @TempDir
  Path tempDir;

  @Test
  void normalizeKeepsLeadingParentForRelativeAndDropsItForAbsolute() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystem fs = new TarFileSystem(
        new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    // Relative: unresolvable ".." must be preserved.
    assertEquals("../a", new TarPath(fs, false, "../a").normalize().toString());
    assertEquals("..", new TarPath(fs, false, "a/../..").normalize().toString());

    // Absolute: ".." above the root must be dropped.
    assertEquals("/a", new TarPath(fs, true, "../a").normalize().toString());
  }
}
