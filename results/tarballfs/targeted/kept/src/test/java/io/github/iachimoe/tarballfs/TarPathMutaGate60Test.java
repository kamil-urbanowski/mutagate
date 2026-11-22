package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate60Test {

  @Test
  void resolveReturnsAbsoluteArgumentUnchangedAndAppendsRelativeOne() throws Exception {
    Path tar = Files.createTempFile("tarpath-resolve", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath()), tar);

      TarPath base = new TarPath(fs, false, "a");
      TarPath absoluteOther = new TarPath(fs, true, "b/c");
      TarPath relativeOther = new TarPath(fs, false, "d");

      Path resolvedAbsolute = base.resolve(absoluteOther);
      assertSame(absoluteOther, resolvedAbsolute);
      assertEquals("/b/c", resolvedAbsolute.toString());

      Path resolvedRelative = base.resolve(relativeOther);
      assertEquals("a/d", resolvedRelative.toString());
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
