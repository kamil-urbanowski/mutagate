package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

final class TarFileSystemMutaGate6Test {
  @Test
  void constructor_storesProviderTarPathAndCreatesFileStore() {
    TarFileSystemProvider provider = new TarFileSystemProvider();
    Path tarPath = Path.of("some-nonexistent-dir", "archive.tar");
    URI uri = URI.create("tar:" + tarPath.toAbsolutePath());

    TarFileSystem fs = new TarFileSystem(provider, uri, tarPath);

    assertSame(provider, fs.provider());
    assertSame(tarPath, fs.tarFile());
    assertEquals("tar:" + tarPath, fs.toString());
    assertTrue(fs.isOpen());

    TarFileStore store = fs.tarFileStore();
    assertNotNull(store);
    assertSame(store, fs.tarFileStore());
    assertSame(store, fs.getFileStores().iterator().next());
  }
}
