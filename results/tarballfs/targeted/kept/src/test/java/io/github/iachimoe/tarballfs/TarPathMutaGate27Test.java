package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate27Test {

  @TempDir
  Path tempDir;

  @Test
  void getNameReturnsRelativeSingleSegmentPath() throws Exception {
    Path tar = tempDir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    URI uri = URI.create("tar:" + tar.toAbsolutePath() + "!");
    TarFileSystem fs = new TarFileSystem(provider, uri, tar);

    TarPath path = new TarPath(fs, true, "a/b/c");

    Path first = path.getName(0);
    Path second = path.getName(1);
    Path third = path.getName(2);

    assertNotNull(first);
    assertNotNull(second);
    assertNotNull(third);
    assertEquals("a", first.toString());
    assertEquals("b", second.toString());
    assertEquals("c", third.toString());
    assertFalse(second.isAbsolute());
    assertEquals(1, second.getNameCount());
    assertSame(fs, second.getFileSystem());
    assertEquals(new TarPath(fs, false, "b"), second);
  }
}
