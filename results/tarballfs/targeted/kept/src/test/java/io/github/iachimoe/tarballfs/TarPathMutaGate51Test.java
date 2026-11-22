package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate51Test {

  @Test
  void relativizeAcceptsMatchingFormAndRejectsMismatchedOrForeignPaths() throws Exception {
    Path tar = Files.createTempFile("mutagate51", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), URI.create("tar:" + tar.toAbsolutePath() + "!"), tar);

      TarPath relBase = new TarPath(fs, false, "a/b");
      TarPath relChild = new TarPath(fs, false, "a/b/c");
      TarPath relOther = new TarPath(fs, false, "a/c");
      TarPath absBase = new TarPath(fs, true, "a/b");
      TarPath absChild = new TarPath(fs, true, "a/b/c");

      assertEquals(new TarPath(fs, false, "c"), relBase.relativize(relChild));
      assertEquals("c", relBase.relativize(relChild).toString());
      assertEquals("../c", relBase.relativize(relOther).toString());
      assertEquals("c", absBase.relativize(absChild).toString());

      assertThrows(IllegalArgumentException.class, () -> relBase.relativize(absChild));
      assertThrows(IllegalArgumentException.class, () -> absBase.relativize(relChild));
      assertThrows(IllegalArgumentException.class, () -> relBase.relativize(Path.of("a/b/c")));
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
