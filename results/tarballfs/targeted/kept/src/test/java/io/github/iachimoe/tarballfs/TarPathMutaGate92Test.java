package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarPathMutaGate92Test {

  @TempDir
  Path tempDir;

  @Test
  void toUriForRootAndEntry() throws Exception {
    Path tar = tempDir.resolve("archive.tar");
    Files.write(tar, new byte[1024]);

    TarFileSystemProvider provider = new TarFileSystemProvider();
    TarFileSystem fs = new TarFileSystem(provider, URI.create("tar:" + tar.toAbsolutePath()), tar);

    String archive = tar.toAbsolutePath().toString();

    URI rootUri = new TarPath(fs, true, "").toUri();
    assertNotNull(rootUri);
    assertEquals(URI.create("tar:" + archive + "!"), rootUri);

    URI entryUri = new TarPath(fs, true, "dir/file.txt").toUri();
    assertNotNull(entryUri);
    assertEquals(URI.create("tar:" + archive + "!dir/file.txt"), entryUri);
  }
}
