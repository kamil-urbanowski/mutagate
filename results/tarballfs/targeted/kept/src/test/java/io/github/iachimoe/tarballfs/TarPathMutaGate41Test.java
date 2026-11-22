package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate41Test {

  @TempDir
  Path tempDir;

  @Test
  void emptySegmentsInTheMiddleOfAPathAreDropped() throws Exception {
    Path tar = tempDir.resolve("archive.tar");
    Files.write(tar, new byte[1024]);
    URI uri = URI.create("tar:" + tar.toAbsolutePath() + "!");
    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), uri, tar);

    TarPath path = new TarPath(fs, false, "a//b");

    assertEquals(2, path.getNameCount());
    assertEquals("a/b", path.entryName());
    assertEquals("a/b", path.toString());
    assertEquals("b", path.getName(1).toString());
    assertEquals("b", path.getFileName().toString());
  }
}
