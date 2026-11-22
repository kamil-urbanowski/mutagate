package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate58Test {

  @Test
  void relativizeReturnsRelativePathWithParentAndChildSegments(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("test.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath base = new TarPath(fs, true, "a/b");
    TarPath target = new TarPath(fs, true, "a/c/d");

    Path result = base.relativize(target);

    assertNotNull(result);
    assertEquals("../c/d", result.toString());
    assertFalse(result.isAbsolute());
    assertEquals(new TarPath(fs, false, "../c/d"), result);
    assertEquals(3, result.getNameCount());
  }
}
