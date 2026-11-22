package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate76Test {

  @Test
  void startsWithHandlesPrefixLengthComparison(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("archive.tar");
    Files.write(tar, new byte[0]);
    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath longer = new TarPath(fs, true, "a/b");
    TarPath shorter = new TarPath(fs, true, "a");
    TarPath same = new TarPath(fs, true, "a/b");

    // prefix shorter than path: must be true
    assertTrue(longer.startsWith(shorter));
    // equal length: must be true
    assertTrue(longer.startsWith(same));
    // prefix longer than path: must be false
    assertFalse(shorter.startsWith(longer));
  }
}
