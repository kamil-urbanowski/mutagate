package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AbstractCompressorArchiveFileSystemMutaGate2Test {

  private static final class TestFileSystem extends AbstractCompressorArchiveFileSystem {
    final List<URI> deregistered = new ArrayList<>();

    TestFileSystem(FileSystemProvider provider, URI uri, Path archive, Path tempFile) {
      super(provider, uri, archive, "gzip", "entry.txt", tempFile);
    }

    @Override
    protected void deregister(URI uri) {
      deregistered.add(uri);
    }

    @Override
    protected SingleEntryArchiveFormat pathFormat() {
      return SingleEntryArchiveFormat.GZIP;
    }
  }

  @Test
  void closeStillDeregistersWhenDeleteFailsAndSecondCloseIsNoOp(@TempDir Path dir) throws IOException {
    Path archive = dir.resolve("a.gz");
    Files.writeString(archive, "x");

    // A non-empty directory cannot be deleted by Files.deleteIfExists
    Path tempDirAsFile = dir.resolve("tmpdir");
    Files.createDirectory(tempDirAsFile);
    Files.writeString(tempDirAsFile.resolve("child"), "data");

    URI uri = URI.create("gzip:" + archive.toUri());
    TestFileSystem fs =
        new TestFileSystem(FileSystems.getDefault().provider(), uri, archive, tempDirAsFile);

    assertTrue(fs.isOpen());

    assertThrows(IOException.class, fs::close);

    assertFalse(fs.isOpen());
    assertEquals(List.of(uri), fs.deregistered);
    assertTrue(Files.exists(tempDirAsFile.resolve("child")));

    // Second close returns immediately without throwing or deregistering again
    fs.close();
    assertEquals(1, fs.deregistered.size());
    assertFalse(fs.isOpen());
  }
}
