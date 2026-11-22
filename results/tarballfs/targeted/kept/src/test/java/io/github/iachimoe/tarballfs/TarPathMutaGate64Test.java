package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate64Test {

  @Test
  void resolveWithAbsoluteOtherReturnsOther() throws Exception {
    Path tar = Files.createTempFile("tarpath-resolve", ".tar");
    tar.toFile().deleteOnExit();
    Files.write(tar, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

    TarPath base = new TarPath(fs, false, "x/y");
    TarPath absolute = new TarPath(fs, true, "a/b");

    Path result = base.resolve(absolute);

    assertNotNull(result);
    assertSame(absolute, result);
    assertEquals("/a/b", result.toString());
  }
}
