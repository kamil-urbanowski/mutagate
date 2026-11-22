package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import org.junit.jupiter.api.Test;

class TarPathMutaGate35Test {

  @Test
  void hashCodeCombinesFileSystemAbsoluteFlagAndSegments() throws Exception {
    Path dir = Files.createTempDirectory("tarpath-hash");
    Path tar = dir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);
    try {
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

      TarPath path = new TarPath(fs, true, "a/b");

      int base = Objects.hash(System.identityHashCode(fs), true);
      int expected = 31 * base + Arrays.hashCode(new String[] {"a", "b"});

      assertEquals(expected, path.hashCode());
    } finally {
      Files.deleteIfExists(tar);
      Files.deleteIfExists(dir);
    }
  }
}
