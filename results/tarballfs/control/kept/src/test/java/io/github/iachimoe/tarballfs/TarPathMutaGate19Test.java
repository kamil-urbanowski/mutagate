package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate19Test {

  @Test
  void endsWithStringBehaviour(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("test.tar");
    Files.write(tar, new byte[1024]);
    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath abs = new TarPath(fs, true, "a/b/c");
    TarPath rel = new TarPath(fs, false, "a/b/c");

    // suffix of name segments
    assertTrue(abs.endsWith("c"));
    assertTrue(abs.endsWith("b/c"));
    assertTrue(abs.endsWith("a/b/c"));
    assertTrue(rel.endsWith("b/c"));

    // non-matching
    assertFalse(abs.endsWith("b"));
    assertFalse(abs.endsWith("a/b"));
    assertFalse(abs.endsWith("x/c"));
    assertFalse(abs.endsWith("d/a/b/c"));

    // partial segment name does not match
    assertFalse(abs.endsWith("bc"));
    assertFalse(abs.endsWith("/c".substring(1) + "x"));

    // trailing slash is normalized away
    assertTrue(abs.endsWith("b/c/"));

    // leading slash is stripped, since the string is parsed as a relative path
    assertTrue(abs.endsWith("/b/c"));

    // empty string has zero segments, so it matches
    assertTrue(abs.endsWith(""));
    assertTrue(new TarPath(fs, true, "").endsWith(""));

    // empty path ends with nothing non-empty
    assertFalse(new TarPath(fs, true, "").endsWith("a"));

    // null rejected
    assertThrows(NullPointerException.class, () -> abs.endsWith((String) null));
  }
}
