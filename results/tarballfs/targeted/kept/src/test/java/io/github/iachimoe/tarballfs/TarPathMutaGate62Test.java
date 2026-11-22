package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate62Test {

  @TempDir
  Path tempDir;

  @Test
  void resolveRelativePathOnSameFileSystemJoinsAndOtherFileSystemIsRejected() throws Exception {
    Path tar1 = Files.createFile(tempDir.resolve("one.tar"));
    Path tar2 = Files.createFile(tempDir.resolve("two.tar"));
    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs1 = new TarFileSystem(provider, tar1.toUri(), tar1);
    TarFileSystem fs2 = new TarFileSystem(provider, tar2.toUri(), tar2);

    TarPath base = new TarPath(fs1, false, "a/b");
    TarPath sameFsRelative = new TarPath(fs1, false, "c");
    TarPath otherFsRelative = new TarPath(fs2, false, "c");

    Path resolved = base.resolve(sameFsRelative);
    assertEquals(new TarPath(fs1, false, "a/b/c"), resolved);

    assertThrows(IllegalArgumentException.class, () -> base.resolve(otherFsRelative));
  }
}
