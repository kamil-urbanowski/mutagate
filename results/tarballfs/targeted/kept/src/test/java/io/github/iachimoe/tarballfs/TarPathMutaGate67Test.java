package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate67Test {

  @TempDir
  Path tempDir;

  @Test
  void resolveSiblingHandlesPathsWithAndWithoutParent() throws Exception {
    Path tar = tempDir.resolve("test.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

    // No parent (relative single segment): falls back to resolve(other) on this path
    TarPath single = new TarPath(fs, false, "a");
    TarPath other = new TarPath(fs, false, "b");
    assertEquals(new TarPath(fs, false, "a/b"), single.resolveSibling(other));

    // Has a parent: resolved against the parent
    TarPath nested = new TarPath(fs, false, "x/a");
    assertEquals(new TarPath(fs, false, "x/b"), nested.resolveSibling(other));
  }
}
