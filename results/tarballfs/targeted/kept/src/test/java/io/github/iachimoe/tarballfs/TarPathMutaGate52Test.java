package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate52Test {

  @Test
  void relativizeRequiresSameFormAndComputesRelativePath() throws Exception {
    Path tar = Files.createTempFile("tarpath-relativize", ".tar");
    tar.toFile().deleteOnExit();
    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);

    TarPath relBase = new TarPath(fs, false, "a/b");
    TarPath relChild = new TarPath(fs, false, "a/b/c");
    TarPath relOther = new TarPath(fs, false, "a/x");
    TarPath absBase = new TarPath(fs, true, "a/b");
    TarPath absChild = new TarPath(fs, true, "a/b/c");

    assertEquals("c", relBase.relativize(relChild).toString());
    assertEquals("../x", relBase.relativize(relOther).toString());
    assertEquals("c", absBase.relativize(absChild).toString());

    assertThrows(IllegalArgumentException.class, () -> relBase.relativize(absChild));
    assertThrows(IllegalArgumentException.class, () -> absBase.relativize(relChild));
  }
}
