package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate5Test {
  @Test
  void pathExistsInTar_coversNullEmptyExactImpliedAndNonMatches() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-mutagate5-", ".tar");
    try {
      byte[] data = "x".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(tarPath);
          TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
        for (String name : new String[] {"root.txt", "dir/sub/leaf.txt", "./dot/b.txt"}) {
          TarArchiveEntry e = new TarArchiveEntry(name);
          e.setSize(data.length);
          tarOut.putArchiveEntry(e);
          tarOut.write(data);
          tarOut.closeArchiveEntry();
        }
      }

      TarFileSystemProvider provider = new TarFileSystemProvider();
      try (FileSystem fs = provider.newFileSystem(tarPath, Map.of())) {
        TarFileSystem tfs = (TarFileSystem) fs;

        assertTrue(TarFileSystemProvider.pathExistsInTar(tfs, null));
        assertTrue(TarFileSystemProvider.pathExistsInTar(tfs, ""));

        assertTrue(TarFileSystemProvider.pathExistsInTar(tfs, "root.txt"));
        assertTrue(TarFileSystemProvider.pathExistsInTar(tfs, "dir/sub/leaf.txt"));

        assertTrue(TarFileSystemProvider.pathExistsInTar(tfs, "dir"));
        assertTrue(TarFileSystemProvider.pathExistsInTar(tfs, "dir/sub"));
        assertTrue(TarFileSystemProvider.pathExistsInTar(tfs, "dot"));
        assertTrue(TarFileSystemProvider.pathExistsInTar(tfs, "dot/b.txt"));

        assertFalse(TarFileSystemProvider.pathExistsInTar(tfs, "di"));
        assertFalse(TarFileSystemProvider.pathExistsInTar(tfs, "dir/su"));
        assertFalse(TarFileSystemProvider.pathExistsInTar(tfs, "dir/sub/leaf"));
        assertFalse(TarFileSystemProvider.pathExistsInTar(tfs, "root.txt/child"));
        assertFalse(TarFileSystemProvider.pathExistsInTar(tfs, "missing"));
        assertFalse(TarFileSystemProvider.pathExistsInTar(tfs, "sub"));
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
