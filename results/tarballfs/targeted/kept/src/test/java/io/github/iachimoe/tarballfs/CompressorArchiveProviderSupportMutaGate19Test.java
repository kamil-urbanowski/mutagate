package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate19Test {

  @Test
  void pathFromUriThrowsWhenNoFileSystemIsMounted() throws Exception {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    URI uri = new URI(format.uriSchemeId(), null, "/tmp/archive.gz", null);
    Function<URI, AbstractCompressorArchiveFileSystem> resolver = key -> null;

    assertThrows(
        FileSystemNotFoundException.class,
        () -> CompressorArchiveProviderSupport.pathFromUri(uri, format, resolver));
  }
}
