package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate8Test {

  @Test
  void equalsDistinguishesFileSystemAbsoluteAndSegments(@TempDir Path dir) throws Exception {
    Path tar1 = dir.resolve("one.tar");
    Path tar2 = dir.resolve("two.tar");
    Files.write(tar1, new byte[1024]);
    Files.write(tar2, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs1 = new TarFileSystem(provider, URI.create("tar:" + tar1.toAbsolutePath() + "!"), tar1);
    TarFileSystem fs2 = new TarFileSystem(provider, URI.create("tar:" + tar2.toAbsolutePath() + "!"), tar2);

    TarPath a = new TarPath(fs1, true, "a/b");

    // reflexive and equal to a separately constructed identical path
    assertTrue(a.equals(a));
    TarPath same = new TarPath(fs1, true, "a/b");
    assertTrue(a.equals(same));
    assertTrue(same.equals(a));
    assertEquals(a.hashCode(), same.hashCode());

    // normalization of separators in the constructor
    assertTrue(a.equals(new TarPath(fs1, true, "/a/b/")));
    assertTrue(a.equals(new TarPath(fs1, true, "a\\b")));
    assertTrue(a.equals(new TarPath(fs1, true, "a//b")));

    // different absolute flag
    assertFalse(a.equals(new TarPath(fs1, false, "a/b")));
    assertFalse(new TarPath(fs1, false, "a/b").equals(a));

    // different file system, same content
    assertFalse(a.equals(new TarPath(fs2, true, "a/b")));

    // different segments
    assertFalse(a.equals(new TarPath(fs1, true, "a/c")));
    assertFalse(a.equals(new TarPath(fs1, true, "c/b")));
    assertFalse(a.equals(new TarPath(fs1, true, "a")));
    assertFalse(a.equals(new TarPath(fs1, true, "a/b/c")));

    // roots
    assertTrue(new TarPath(fs1, true, "").equals(new TarPath(fs1, true, "/")));
    assertFalse(new TarPath(fs1, true, "").equals(new TarPath(fs1, false, "")));

    // null and foreign types
    assertFalse(a.equals(null));
    assertFalse(a.equals("/a/b"));
    assertFalse(a.equals(new Object()));
  }
}
