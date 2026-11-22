package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate74Test {

  @TempDir
  Path tempDir;

  @Test
  void startsWithAcceptsTarPathAndRejectsForeignPath() throws Exception {
    Path tarFile = tempDir.resolve("empty.tar");
    Files.write(tarFile, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tarFile.toUri(), tarFile);

    TarPath path = new TarPath(fs, true, "a/b");
    TarPath prefix = new TarPath(fs, true, "a");
    TarPath other = new TarPath(fs, true, "c");

    assertTrue(path.startsWith(prefix));
    assertTrue(path.startsWith(path));
    assertFalse(path.startsWith(other));
    assertFalse(path.startsWith(tempDir));
  }
}
