package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate44Test {

  @TempDir
  Path tempDir;

  @Test
  void normalizeCollapsesDotDotAgainstPrecedingSegment() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystemProvider provider = new TarFileSystemProvider();
    URI uri = URI.create("tar:" + tar.toAbsolutePath().toUri().getPath() + "!");
    TarFileSystem fs = new TarFileSystem(provider, uri, tar);

    assertEquals("b", new TarPath(fs, false, "a/../b").normalize().toString());
    assertEquals("", new TarPath(fs, false, "a/..").normalize().toString());
    assertEquals("/c", new TarPath(fs, true, "a/b/../../c").normalize().toString());
    assertEquals("../a", new TarPath(fs, false, "../a").normalize().toString());
  }
}
