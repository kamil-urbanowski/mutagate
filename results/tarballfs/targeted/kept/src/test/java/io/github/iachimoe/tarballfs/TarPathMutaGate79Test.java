package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate79Test {

  @Test
  void startsWithReturnsTrueForMatchingPrefix(@TempDir Path dir) throws IOException {
    Path tar = dir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

    TarPath full = new TarPath(fs, true, "a/b/c");
    TarPath prefix = new TarPath(fs, true, "a/b");
    TarPath other = new TarPath(fs, true, "a/x");

    assertTrue(full.startsWith(prefix));
    assertTrue(full.startsWith(full));
    assertFalse(full.startsWith(other));
  }
}
