package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate3Test {

  @Test
  void archiveKeyUriStripsEntrySuffixMatchesSchemeIgnoringCaseAndRejectsBadInput() {
    URI expected = URI.create("tar:" + Path.of("/tmp/a.tar").toAbsolutePath());

    // Entry suffix after '!' is stripped
    assertEquals(
        expected,
        ArchiveUriSupport.archiveKeyUri(URI.create("tar:///tmp/a.tar!/entry/file"), "tar", "tar:"));

    // No bang: path used as is
    assertEquals(
        expected,
        ArchiveUriSupport.archiveKeyUri(URI.create("tar:///tmp/a.tar"), "tar", "tar:"));

    // Scheme comparison is case-insensitive
    assertEquals(
        expected,
        ArchiveUriSupport.archiveKeyUri(URI.create("TAR:///tmp/a.tar!/e"), "tar", "tar:"));

    // Wrong scheme
    IllegalArgumentException wrongScheme =
        assertThrows(
            IllegalArgumentException.class,
            () -> ArchiveUriSupport.archiveKeyUri(URI.create("zip:///tmp/a.tar"), "tar", "tar:"));
    assertEquals("Unsupported URI scheme: zip", wrongScheme.getMessage());

    // Empty path
    IllegalArgumentException emptyPath =
        assertThrows(
            IllegalArgumentException.class,
            () -> ArchiveUriSupport.archiveKeyUri(URI.create("tar://host"), "tar", "tar:"));
    assertEquals("tar URI does not contain a file path: tar://host", emptyPath.getMessage());
  }
}
