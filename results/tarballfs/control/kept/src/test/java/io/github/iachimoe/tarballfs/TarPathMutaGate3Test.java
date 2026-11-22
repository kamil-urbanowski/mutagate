package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate3Test {

  @Test
  void constructorNormalizesEntryName() throws Exception {
    Path tmp = Files.createTempFile("tarpath-test", ".tar");
    try {
      Files.write(tmp, new byte[1024]);
      TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), URI.create("tar:" + tmp + "!"), tmp);

      TarPath messy = new TarPath(fs, false, "//a\\b//c///");
      assertEquals(3, messy.getNameCount());
      assertEquals("a/b/c", messy.entryName());
      assertEquals("a/b/c", messy.toString());
      assertEquals("a", messy.getName(0).toString());
      assertEquals("b", messy.getName(1).toString());
      assertEquals("c", messy.getName(2).toString());

      TarPath absRoot = new TarPath(fs, true, "");
      assertEquals(0, absRoot.getNameCount());
      assertEquals("/", absRoot.toString());
      assertEquals("", absRoot.entryName());

      TarPath nullName = new TarPath(fs, false, null);
      assertEquals(0, nullName.getNameCount());
      assertEquals("", nullName.toString());

      TarPath onlySlashes = new TarPath(fs, true, "///");
      assertEquals(0, onlySlashes.getNameCount());
      assertEquals("/", onlySlashes.toString());

      TarPath onlyBackslashes = new TarPath(fs, false, "\\\\");
      assertEquals(0, onlyBackslashes.getNameCount());

      TarPath absolute = new TarPath(fs, true, "/x/y/");
      assertEquals("/x/y", absolute.toString());
      assertEquals(2, absolute.getNameCount());

      assertEquals(new TarPath(fs, false, "a/b/c"), messy);
      assertEquals(new TarPath(fs, false, "a/b/c").hashCode(), messy.hashCode());
      assertNotEquals(new TarPath(fs, true, "a/b/c"), messy);

      NullPointerException npe =
          assertThrows(NullPointerException.class, () -> new TarPath(null, false, "a"));
      assertEquals("fileSystem", npe.getMessage());
    } finally {
      Files.deleteIfExists(tmp);
    }
  }
}
