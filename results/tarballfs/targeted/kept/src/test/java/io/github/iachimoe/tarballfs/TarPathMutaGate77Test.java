package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate77Test {

  @Test
  void startsWithComparesEachSegmentOfPrefix() throws Exception {
    Path tar = Files.createTempFile("tarpath-test", ".tar");
    tar.toFile().deleteOnExit();
    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath path = new TarPath(fs, false, "a/b/c");

    assertTrue(path.startsWith(new TarPath(fs, false, "a")));
    assertTrue(path.startsWith(new TarPath(fs, false, "a/b")));
    assertFalse(path.startsWith(new TarPath(fs, false, "x")));
    assertFalse(path.startsWith(new TarPath(fs, false, "a/x")));
  }
}
