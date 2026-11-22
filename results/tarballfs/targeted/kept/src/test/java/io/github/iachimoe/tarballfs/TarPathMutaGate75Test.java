package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate75Test {

  @Test
  void startsWithRequiresMatchingAbsoluteForm(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("empty.tar");
    Files.write(tar, new byte[10240]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

    TarPath absAB = new TarPath(fs, true, "a/b");
    TarPath absA = new TarPath(fs, true, "a");
    TarPath relA = new TarPath(fs, false, "a");
    TarPath relAB = new TarPath(fs, false, "a/b");
    TarPath relPrefix = new TarPath(fs, false, "a");

    // Same absolute form: prefix matches
    assertTrue(absAB.startsWith(absA));
    assertTrue(relAB.startsWith(relPrefix));

    // Different absolute form: must not match even though segments are a prefix
    assertFalse(absAB.startsWith(relA));
    assertFalse(relAB.startsWith(absA));
  }
}
