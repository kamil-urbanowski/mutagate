package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate23Test {

  @TempDir
  Path tempDir;

  @Test
  void getRootReturnsRootOnlyForAbsolutePaths() throws Exception {
    Path tar = tempDir.resolve("archive.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath absolute = new TarPath(fs, true, "a/b/c.txt");
    Path root = absolute.getRoot();
    assertNotNull(root);
    assertTrue(root.isAbsolute());
    assertEquals(new TarPath(fs, true, ""), root);
    assertEquals("/", root.toString());
    assertEquals(0, root.getNameCount());
    assertSame(fs, root.getFileSystem());
    assertEquals(root, root.getRoot());

    assertEquals(root, new TarPath(fs, true, "").getRoot());

    assertNull(new TarPath(fs, false, "a/b/c.txt").getRoot());
    assertNull(new TarPath(fs, false, "").getRoot());
  }
}
