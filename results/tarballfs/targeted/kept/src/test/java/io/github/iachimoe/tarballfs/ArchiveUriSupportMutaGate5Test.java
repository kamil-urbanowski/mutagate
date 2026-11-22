package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate5Test {

  @Test
  void archiveKeyUriStripsEntrySuffixAndKeepsPlainPath() {
    URI withEntry = URI.create("tarballfs:/tmp/archive.tar!/some/entry.txt");
    URI plain = URI.create("tarballfs:/tmp/archive.tar");

    URI expected = URI.create("tarballfs:" + Path.of("/tmp/archive.tar").toAbsolutePath());

    assertEquals(expected, ArchiveUriSupport.archiveKeyUri(withEntry, "tarballfs", "tarballfs:"));
    assertEquals(expected, ArchiveUriSupport.archiveKeyUri(plain, "tarballfs", "tarballfs:"));
  }
}
