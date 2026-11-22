package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class TarPathMutaGate31Test {

  @Test
  void getParentHandlesSingleAndMultipleSegments() throws Exception {
    Path tar = Files.createTempFile("tarpath-muta", ".tar");
    try {
      Files.write(tar, new byte[1024]);
      TarFileSystemProvider provider = new TarFileSystemProvider();
      TarFileSystem fs = new TarFileSystem(provider, tar.toUri(), tar);

      TarPath relativeSingle = new TarPath(fs, false, "a");
      assertNull(relativeSingle.getParent());

      TarPath absoluteSingle = new TarPath(fs, true, "a");
      assertEquals(new TarPath(fs, true, ""), absoluteSingle.getParent());

      TarPath relativeDouble = new TarPath(fs, false, "a/b");
      assertEquals(new TarPath(fs, false, "a"), relativeDouble.getParent());

      TarPath absoluteDouble = new TarPath(fs, true, "a/b");
      assertEquals(new TarPath(fs, true, "a"), absoluteDouble.getParent());
    } finally {
      Files.deleteIfExists(tar);
    }
  }
}
