package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate18Test {

  @Test
  void startsWithStringBehavior() throws Exception {
    Path tmp = Files.createTempFile("tarpath-test", ".tar");
    try {
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tmp.toUri()), tmp);

      TarPath relative = new TarPath(fs, false, "a/b/c");
      TarPath absolute = new TarPath(fs, true, "a/b/c");

      // relative path with relative string prefixes
      assertTrue(relative.startsWith("a"));
      assertTrue(relative.startsWith("a/b"));
      assertTrue(relative.startsWith("a/b/c"));
      assertTrue(relative.startsWith("a/b/"));

      // non-matching or partial-segment prefixes
      assertFalse(relative.startsWith("b"));
      assertFalse(relative.startsWith("a/c"));
      assertFalse(relative.startsWith("a/b/c/d"));
      assertFalse(relative.startsWith("ab"));

      // the string is always parsed as a relative path (leading slash stripped)
      assertTrue(relative.startsWith("/a/b"));

      // absolute path never starts with a string-derived (relative) path
      assertFalse(absolute.startsWith("a"));
      assertFalse(absolute.startsWith("/a"));
      assertFalse(absolute.startsWith("a/b/c"));

      // empty string yields zero segments, relative
      assertTrue(relative.startsWith(""));
      assertFalse(absolute.startsWith(""));

      // null argument rejected
      assertThrows(NullPointerException.class, () -> relative.startsWith((String) null));
    } finally {
      Files.deleteIfExists(tmp);
    }
  }
}
