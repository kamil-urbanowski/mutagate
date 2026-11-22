package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate78Test {

  @TempDir
  Path tempDir;

  @Test
  void startsWithComparesSegmentsOneByOne() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    URI uri = URI.create("tar:" + tar.toAbsolutePath() + "!");
    TarFileSystem fs = new TarFileSystem(provider, uri, tar);

    TarPath full = new TarPath(fs, true, "a/b/c");
    TarPath matchingPrefix = new TarPath(fs, true, "a/b");
    TarPath differingPrefix = new TarPath(fs, true, "a/x");
    TarPath differingFirst = new TarPath(fs, true, "z/b");

    assertTrue(full.startsWith(matchingPrefix));
    assertTrue(full.startsWith(full));
    assertFalse(full.startsWith(differingPrefix));
    assertFalse(full.startsWith(differingFirst));
  }
}
