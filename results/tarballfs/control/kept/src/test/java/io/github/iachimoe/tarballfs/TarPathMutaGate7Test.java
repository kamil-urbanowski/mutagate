package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate7Test {

  @TempDir
  Path tempDir;

  @Test
  void endsWithComparesTrailingSegmentsOnly() throws Exception {
    Path tar = tempDir.resolve("test.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath abs = new TarPath(fs, true, "a/b/c");
    TarPath rel = new TarPath(fs, false, "a/b/c");

    // suffix of segments matches
    assertTrue(abs.endsWith(new TarPath(fs, false, "c")));
    assertTrue(abs.endsWith(new TarPath(fs, false, "b/c")));
    assertTrue(abs.endsWith(new TarPath(fs, false, "a/b/c")));

    // absoluteness of other is not considered by endsWith
    assertTrue(abs.endsWith(new TarPath(fs, true, "a/b/c")));
    assertTrue(rel.endsWith(new TarPath(fs, true, "a/b/c")));
    assertTrue(rel.endsWith(new TarPath(fs, true, "b/c")));

    // empty other has no segments, so it matches
    assertTrue(abs.endsWith(new TarPath(fs, false, "")));
    assertTrue(rel.endsWith(new TarPath(fs, true, "")));

    // string overload
    assertTrue(abs.endsWith("b/c"));
    assertFalse(abs.endsWith("a/b"));

    // mismatches
    assertFalse(abs.endsWith(new TarPath(fs, false, "b")));
    assertFalse(abs.endsWith(new TarPath(fs, false, "a/c")));
    assertFalse(abs.endsWith(new TarPath(fs, false, "x/b/c")));

    // partial segment name does not match
    assertFalse(new TarPath(fs, false, "ab").endsWith(new TarPath(fs, false, "b")));

    // other longer than this
    assertFalse(new TarPath(fs, false, "b/c").endsWith(new TarPath(fs, false, "a/b/c")));
    assertFalse(new TarPath(fs, true, "").endsWith(new TarPath(fs, false, "a")));

    // non-TarPath argument
    assertFalse(abs.endsWith(Path.of("c")));
  }
}
