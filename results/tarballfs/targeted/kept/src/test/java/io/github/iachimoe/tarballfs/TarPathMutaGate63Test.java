package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate63Test {

  @TempDir
  Path tempDir;

  @Test
  void resolveAppendsRelativePathToNonEmptyBase() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    URI uri = URI.create("tar:" + tar.toAbsolutePath() + "!");
    TarFileSystem fs = new TarFileSystem(provider, uri, tar);

    TarPath base = new TarPath(fs, false, "a");
    TarPath other = new TarPath(fs, false, "b");

    Path resolved = base.resolve(other);

    assertEquals("a/b", resolved.toString());
    assertEquals(new TarPath(fs, false, "a/b"), resolved);

    Path absResolved = new TarPath(fs, true, "x/y").resolve("z");
    assertEquals("/x/y/z", absResolved.toString());
  }
}
