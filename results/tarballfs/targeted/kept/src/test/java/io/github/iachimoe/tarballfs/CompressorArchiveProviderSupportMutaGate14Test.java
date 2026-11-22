package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate14Test {

  @Test
  void pathFromUriRejectsUriWithDifferentScheme() {
    URI uri = URI.create("bogus-scheme:/tmp/archive.gz");

    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                CompressorArchiveProviderSupport.pathFromUri(
                    uri, SingleEntryArchiveFormat.GZIP, u -> null));

    assertEquals("Unsupported URI scheme: bogus-scheme", ex.getMessage());
  }
}
