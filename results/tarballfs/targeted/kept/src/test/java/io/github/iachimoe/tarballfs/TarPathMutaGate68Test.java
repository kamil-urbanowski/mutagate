package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate68Test {

  @Test
  void resolveSiblingOnSingleSegmentRelativePathResolvesAgainstItself(@TempDir Path dir) throws Exception {
    Path tar = dir.resolve("test.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystem fs = new TarFileSystem(new TarFileSystemProvider(), tar.toUri(), tar);
    try {
      TarPath single = new TarPath(fs, false, "a");
      TarPath other = new TarPath(fs, false, "b");

      Path result = single.resolveSibling((Path) other);

      assertNotNull(result);
      assertEquals("a/b", result.toString());
    } finally {
      fs.close();
    }
  }
}
