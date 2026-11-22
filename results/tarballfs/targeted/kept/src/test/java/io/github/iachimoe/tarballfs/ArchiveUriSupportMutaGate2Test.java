package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate2Test {

  @Test
  void archiveKeyUriAcceptsMatchingSchemeAndRejectsOthers() {
    URI matching = URI.create("TARBALL:/tmp/a.tar!/entry");
    URI result = ArchiveUriSupport.archiveKeyUri(matching, "tarball", "tarball:");
    assertEquals(URI.create("tarball:" + Path.of("/tmp/a.tar").toAbsolutePath()), result);

    URI other = URI.create("zip:/tmp/a.tar");
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> ArchiveUriSupport.archiveKeyUri(other, "tarball", "tarball:"));
    assertEquals("Unsupported URI scheme: zip", ex.getMessage());
  }
}
