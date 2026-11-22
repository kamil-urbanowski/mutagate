package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.junit.jupiter.api.Test;

final class Bzip2FileSystemMutaGate1Test {
  @Test
  void constructor_wiresProviderArchivePathAndFileStoreType() throws IOException {
    Path bzPath = Files.createTempFile("tarballfs-ctor-", ".bz2");
    try {
      byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(bzPath);
          BZip2CompressorOutputStream bzOut = new BZip2CompressorOutputStream(out)) {
        bzOut.write(data);
      }

      Bzip2FileSystemProvider provider = new Bzip2FileSystemProvider();
      URI uri = URI.create("bzip2:" + bzPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        Bzip2FileSystem bzFs = (Bzip2FileSystem) fs;

        assertSame(provider, bzFs.provider());
        assertNotNull(bzFs.bzip2File());
        assertTrue(Files.isSameFile(bzPath, bzFs.bzip2File()));
        assertEquals("bzip2:" + bzFs.bzip2File(), bzFs.toString());

        assertNotNull(bzFs.bzip2FileStore());
        assertEquals("bzip2", bzFs.bzip2FileStore().type());
        assertTrue(bzFs.bzip2FileStore().isReadOnly());
        assertTrue(bzFs.isOpen());
        assertTrue(bzFs.isReadOnly());
      }
    } finally {
      Files.deleteIfExists(bzPath);
    }
  }
}
