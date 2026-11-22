package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TarFileStoreMutaGate3Test {

  @TempDir
  Path tempDir;

  @Test
  void nameReturnsTarFileName() throws Exception {
    Path tar = tempDir.resolve("sample-archive.tar");
    Files.write(tar, new byte[0]);

    TarFileSystem fileSystem =
        new TarFileSystem(new TarFileSystemProvider(), URI.create("tarball:" + tar.toUri()), tar);
    TarFileStore store = new TarFileStore(fileSystem);

    assertEquals("sample-archive.tar", store.name());
  }
}
