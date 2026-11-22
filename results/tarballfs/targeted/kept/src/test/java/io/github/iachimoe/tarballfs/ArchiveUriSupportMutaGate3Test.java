package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate3Test {

  @Test
  void archiveKeyUriAcceptsPathAndRejectsMissingPath() {
    URI valid = URI.create("tarballfs:/tmp/some-archive.tar!/entry/file.txt");
    URI result = ArchiveUriSupport.archiveKeyUri(valid, "tarballfs", "tarballfs:");
    URI expected =
        URI.create("tarballfs:" + Path.of("/tmp/some-archive.tar").toAbsolutePath());
    assertEquals(expected, result);

    URI opaque = URI.create("tarballfs:opaque");
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> ArchiveUriSupport.archiveKeyUri(opaque, "tarballfs", "tarballfs:"));
    assertEquals("tarballfs URI does not contain a file path: " + opaque, ex.getMessage());
  }
}
