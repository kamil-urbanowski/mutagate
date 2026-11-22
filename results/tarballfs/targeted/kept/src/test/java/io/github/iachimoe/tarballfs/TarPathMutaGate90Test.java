package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate90Test {

  @Test
  void subpathReturnsExpectedRelativePath(@TempDir Path tempDir) throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath path = new TarPath(fs, true, "a/b/c/d");
    Path sub = path.subpath(1, 3);

    assertNotNull(sub);
    assertEquals("b/c", sub.toString());
    assertFalse(sub.isAbsolute());
    assertEquals(2, sub.getNameCount());
    assertEquals(new TarPath(fs, false, "b/c"), sub);
  }
}
