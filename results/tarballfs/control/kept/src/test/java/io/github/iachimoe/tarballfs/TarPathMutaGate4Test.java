package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate4Test {

  @Test
  void startsWithBehaviors(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("test.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath abs = new TarPath(fs, true, "a/b/c");
    TarPath rel = new TarPath(fs, false, "a/b/c");

    // proper prefixes
    assertTrue(abs.startsWith(new TarPath(fs, true, "a")));
    assertTrue(abs.startsWith(new TarPath(fs, true, "a/b")));
    assertTrue(rel.startsWith(new TarPath(fs, false, "a/b")));

    // equal path
    assertTrue(abs.startsWith(new TarPath(fs, true, "a/b/c")));
    assertTrue(rel.startsWith(new TarPath(fs, false, "a/b/c")));

    // root / empty prefixes
    assertTrue(abs.startsWith(new TarPath(fs, true, "")));
    assertTrue(rel.startsWith(new TarPath(fs, false, "")));
    assertTrue(new TarPath(fs, true, "").startsWith(new TarPath(fs, true, "")));

    // absolute/relative mismatch
    assertFalse(abs.startsWith(new TarPath(fs, false, "a")));
    assertFalse(rel.startsWith(new TarPath(fs, true, "a")));
    assertFalse(abs.startsWith(new TarPath(fs, false, "")));

    // other longer than this
    assertFalse(new TarPath(fs, true, "a/b").startsWith(abs));
    assertFalse(new TarPath(fs, true, "").startsWith(new TarPath(fs, true, "a")));

    // differing segments
    assertFalse(abs.startsWith(new TarPath(fs, true, "b")));
    assertFalse(abs.startsWith(new TarPath(fs, true, "a/x")));
    assertFalse(abs.startsWith(new TarPath(fs, true, "a/b/x")));

    // segment-wise, not string prefix
    assertFalse(new TarPath(fs, true, "ab/c").startsWith(new TarPath(fs, true, "a")));

    // non-TarPath argument
    assertFalse(abs.startsWith(Path.of("a")));
    assertFalse(rel.startsWith(Path.of("a")));
  }
}
