package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate73Test {

  @TempDir
  Path tempDir;

  @Test
  void startsWithMatchesPrefixAndEqualPathsWithoutOutOfBounds() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    // An empty tar archive is two zero-filled 512-byte blocks.
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(
        new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath full = new TarPath(fs, true, "a/b/c");
    TarPath prefix = new TarPath(fs, true, "a/b");
    TarPath same = new TarPath(fs, true, "a/b/c");
    TarPath other = new TarPath(fs, true, "a/x");

    assertTrue(full.startsWith(prefix));
    assertTrue(full.startsWith(same));
    assertFalse(full.startsWith(other));
    assertFalse(prefix.startsWith(full));
  }
}
