package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate17Test {

  @Test
  void pathFromUriWithoutBangUsesWholePathAsArchiveKey() {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    String prefix = format.uriSchemePrefix();
    URI uri = URI.create(prefix + "/tmp/mutagate17-archive.gz");

    AtomicReference<URI> captured = new AtomicReference<>();

    assertThrows(
        FileSystemNotFoundException.class,
        () ->
            CompressorArchiveProviderSupport.pathFromUri(
                uri,
                format,
                key -> {
                  captured.set(key);
                  return null;
                }));

    URI expected = URI.create(prefix + Path.of("/tmp/mutagate17-archive.gz").toAbsolutePath());
    assertEquals(expected, captured.get());
  }
}
