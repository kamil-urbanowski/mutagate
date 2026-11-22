package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate37Test {

  @Test
  void hashCodeCombinesFileSystemAbsoluteFlagAndSegments(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("archive.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath path = new TarPath(fs, true, "a/b");

    int expected = 31 * Objects.hash(System.identityHashCode(fs), true)
        + Arrays.hashCode(new String[] {"a", "b"});

    assertEquals(expected, path.hashCode());
    assertEquals(path.hashCode(), new TarPath(fs, true, "/a/b/").hashCode());
  }
}
