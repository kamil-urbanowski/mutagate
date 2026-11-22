package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.junit.jupiter.api.Test;

final class TarFileSystemProviderMutaGate6Test {
  @Test
  void checkAccess_modesAndExistenceCombinations() throws IOException {
    Path tarPath = Files.createTempFile("tarballfs-test-", ".tar");
    try {
      byte[] data = "inner".getBytes(StandardCharsets.UTF_8);
      try (OutputStream out = Files.newOutputStream(tarPath);
          TarArchiveOutputStream tarOut = new TarArchiveOutputStream(out)) {
        TarArchiveEntry entry = new TarArchiveEntry("dir/inner.txt");
        entry.setSize(data.length);
        tarOut.putArchiveEntry(entry);
        tarOut.write(data);
        tarOut.closeArchiveEntry();
      }

      TarFileSystemProvider provider = new TarFileSystemProvider();
      URI uri = URI.create("tar:" + tarPath.toAbsolutePath());
      try (FileSystem fs = provider.newFileSystem(uri, Map.of())) {
        Path file = fs.getPath("/dir/inner.txt");
        Path impliedDir = fs.getPath("/dir");
        Path root = fs.getPath("/");
        Path missing = fs.getPath("/dir/missing.txt");

        // No modes: existing paths succeed.
        assertDoesNotThrow(() -> provider.checkAccess(file));
        assertDoesNotThrow(() -> provider.checkAccess(impliedDir));
        assertDoesNotThrow(() -> provider.checkAccess(root));

        // Null modes array behaves like no modes.
        assertDoesNotThrow(() -> provider.checkAccess(file, (AccessMode[]) null));

        // READ succeeds on files, implied directories and root.
        assertDoesNotThrow(() -> provider.checkAccess(file, AccessMode.READ));
        assertDoesNotThrow(() -> provider.checkAccess(impliedDir, AccessMode.READ));
        assertDoesNotThrow(() -> provider.checkAccess(root, AccessMode.READ));

        // Any non-READ mode is denied, even when combined with READ.
        assertThrows(AccessDeniedException.class,
            () -> provider.checkAccess(file, AccessMode.EXECUTE));
        assertThrows(AccessDeniedException.class,
            () -> provider.checkAccess(file, AccessMode.READ, AccessMode.WRITE));
        assertThrows(AccessDeniedException.class,
            () -> provider.checkAccess(impliedDir, AccessMode.WRITE));
        assertThrows(AccessDeniedException.class,
            () -> provider.checkAccess(root, AccessMode.EXECUTE));

        // Missing paths fail with NoSuchFileException regardless of modes.
        assertThrows(NoSuchFileException.class, () -> provider.checkAccess(missing));
        assertThrows(NoSuchFileException.class,
            () -> provider.checkAccess(missing, AccessMode.EXECUTE));
      }
    } finally {
      Files.deleteIfExists(tarPath);
    }
  }
}
