package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate43Test {

  @Test
  void normalizeRemovesPrecedingSegmentForDotDot(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("empty.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);

    TarPath relative = new TarPath(fs, false, "a/b/../c");
    Path normalizedRelative = relative.normalize();
    assertEquals("a/c", normalizedRelative.toString());
    assertEquals(2, normalizedRelative.getNameCount());

    TarPath absolute = new TarPath(fs, true, "x/y/../z");
    assertEquals("/x/z", absolute.normalize().toString());
  }
}
