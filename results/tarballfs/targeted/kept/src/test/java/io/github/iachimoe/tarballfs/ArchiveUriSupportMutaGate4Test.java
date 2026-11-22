package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate4Test {

  @Test
  void archiveKeyUriAcceptsNonEmptyPathAndRejectsMissingOrEmptyPath() {
    URI valid = URI.create("tar:/tmp/some-archive.tar!/entry/file.txt");
    URI expected = URI.create("tar:" + Path.of("/tmp/some-archive.tar").toAbsolutePath());
    assertEquals(expected, ArchiveUriSupport.archiveKeyUri(valid, "tar", "tar:"));

    // Opaque URI: getPath() returns null
    URI nullPath = URI.create("tar:opaque");
    assertThrows(
        IllegalArgumentException.class,
        () -> ArchiveUriSupport.archiveKeyUri(nullPath, "tar", "tar:"));

    // Authority only: getPath() returns an empty string
    URI emptyPath = URI.create("tar://host");
    assertThrows(
        IllegalArgumentException.class,
        () -> ArchiveUriSupport.archiveKeyUri(emptyPath, "tar", "tar:"));
  }
}
