package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate17Test {

  @Test
  void hashCodeFollowsFormulaAndNormalization(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("a.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath abs = new TarPath(fs, true, "a/b");
    TarPath rel = new TarPath(fs, false, "a/b");

    int expectedAbs = 31 * Objects.hash(System.identityHashCode(fs), true)
        + Arrays.hashCode(new String[] {"a", "b"});
    int expectedRel = 31 * Objects.hash(System.identityHashCode(fs), false)
        + Arrays.hashCode(new String[] {"a", "b"});

    assertEquals(expectedAbs, abs.hashCode());
    assertEquals(expectedRel, rel.hashCode());
    assertNotEquals(abs.hashCode(), rel.hashCode());

    // normalization of separators gives same segments and same hash
    assertEquals(abs.hashCode(), new TarPath(fs, true, "/a//b/").hashCode());
    assertEquals(abs.hashCode(), new TarPath(fs, true, "a\\b").hashCode());

    // different segments produce different hashes
    assertNotEquals(abs.hashCode(), new TarPath(fs, true, "a/c").hashCode());

    // root vs empty relative path
    assertNotEquals(new TarPath(fs, true, "").hashCode(), new TarPath(fs, false, "").hashCode());
    assertEquals(31 * Objects.hash(System.identityHashCode(fs), true) + Arrays.hashCode(new String[0]),
        new TarPath(fs, true, null).hashCode());
  }
}
