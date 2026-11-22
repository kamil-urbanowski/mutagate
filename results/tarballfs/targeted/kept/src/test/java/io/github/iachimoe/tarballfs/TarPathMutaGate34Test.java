package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate34Test {

  @Test
  void getRootReturnsRootForAbsoluteAndNullForRelative() throws Exception {
    Path tar = Files.createTempFile("tarpath-root", ".tar");
    try {
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

      TarPath absolute = new TarPath(fs, true, "a/b");
      Path root = absolute.getRoot();
      assertNotNull(root);
      assertTrue(root.isAbsolute());
      assertEquals(new TarPath(fs, true, ""), root);
      assertEquals("/", root.toString());

      TarPath relative = new TarPath(fs, false, "a/b");
      assertNull(relative.getRoot());
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
