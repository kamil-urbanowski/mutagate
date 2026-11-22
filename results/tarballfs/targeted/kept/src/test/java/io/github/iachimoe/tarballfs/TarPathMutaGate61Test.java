package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate61Test {

  @Test
  void resolveRelativePathOnSameFileSystemWorksAndOtherFileSystemIsRejected() throws Exception {
    Path tar1 = Files.createTempFile("tarpath-a", ".tar");
    Path tar2 = Files.createTempFile("tarpath-b", ".tar");
    try {
      Files.write(tar1, new byte[1024]);
      Files.write(tar2, new byte[1024]);

      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs1 = new TarFileSystem(provider, tar1.toUri(), tar1);
      TarFileSystem fs2 = new TarFileSystem(provider, tar2.toUri(), tar2);

      TarPath base = new TarPath(fs1, true, "a/b");
      TarPath sameFsRelative = new TarPath(fs1, false, "c/d");

      Path resolved = base.resolve(sameFsRelative);
      assertEquals(new TarPath(fs1, true, "a/b/c/d"), resolved);

      TarPath otherFsRelative = new TarPath(fs2, false, "c/d");
      assertThrows(IllegalArgumentException.class, () -> base.resolve(otherFsRelative));
    } finally {
      Files.deleteIfExists(tar1);
      Files.deleteIfExists(tar2);
    }
  }
}
