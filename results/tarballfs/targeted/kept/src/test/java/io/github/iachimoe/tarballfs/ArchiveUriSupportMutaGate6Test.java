package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate6Test {

  @Test
  void archiveKeyUriStripsEntrySuffixAndReturnsAbsoluteKeyUri() {
    URI uri = URI.create("tarball:/tmp/some/archive.tar!/inner/entry.txt");

    URI result = ArchiveUriSupport.archiveKeyUri(uri, "tarball", "tarball://");

    assertNotNull(result);
    URI expected = URI.create("tarball://" + Path.of("/tmp/some/archive.tar").toAbsolutePath());
    assertEquals(expected, result);
  }
}
