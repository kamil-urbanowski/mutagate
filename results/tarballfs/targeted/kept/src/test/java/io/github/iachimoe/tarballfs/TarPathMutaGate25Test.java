package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate25Test {

  @Test
  void equalsReturnsFalseForNonTarPathObjects(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(
        new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

    TarPath path = new TarPath(fs, true, "a/b");

    assertFalse(path.equals("/a/b"));
    assertFalse(path.equals(null));
    assertFalse(path.equals(new Object()));
    assertFalse(path.equals(tar));

    TarPath same = new TarPath(fs, true, "a/b");
    assertTrue(path.equals(same));
    assertEquals(path.hashCode(), same.hashCode());
    assertFalse(path.equals(new TarPath(fs, false, "a/b")));
    assertFalse(path.equals(new TarPath(fs, true, "a/c")));
  }
}
