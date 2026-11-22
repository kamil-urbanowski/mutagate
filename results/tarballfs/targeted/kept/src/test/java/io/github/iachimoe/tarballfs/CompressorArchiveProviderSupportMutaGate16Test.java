package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate16Test {

  @Test
  void pathFromUriRejectsUriWithoutPath() {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    String scheme = format.uriSchemeId();

    // Opaque URI: getPath() returns null
    URI opaque = URI.create(scheme + ":x");
    IllegalArgumentException ex1 = assertThrows(
        IllegalArgumentException.class,
        () -> CompressorArchiveProviderSupport.pathFromUri(
            opaque,
            format,
            u -> {
              throw new AssertionError("resolver must not be called");
            }));
    assertEquals(scheme + " URI must contain a path: " + opaque, ex1.getMessage());

    // Hierarchical URI with empty path
    URI emptyPath = URI.create(scheme + "://host");
    IllegalArgumentException ex2 = assertThrows(
        IllegalArgumentException.class,
        () -> CompressorArchiveProviderSupport.pathFromUri(
            emptyPath,
            format,
            u -> {
              throw new AssertionError("resolver must not be called");
            }));
    assertEquals(scheme + " URI must contain a path: " + emptyPath, ex2.getMessage());
  }
}
