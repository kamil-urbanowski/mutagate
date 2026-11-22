package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate15Test {

  @Test
  void opaqueUriWithoutPathIsRejectedWithPathMessage() throws Exception {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    String scheme = format.uriSchemeId();
    // Opaque URI: scheme matches, but getPath() returns null.
    URI uri = new URI(scheme, "opaque-part", null);

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                CompressorArchiveProviderSupport.pathFromUri(
                    uri,
                    format,
                    key -> {
                      throw new AssertionError("resolver must not be called");
                    }));

    assertEquals(scheme + " URI must contain a path: " + uri, ex.getMessage());
  }
}
