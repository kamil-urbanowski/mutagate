package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CompressorArchiveProviderSupportMutaGate1Test {

  @Test
  void pathFromUriBuildsArchiveKeyAndReportsUnmountedFileSystem() {
    SingleEntryArchiveFormat format = SingleEntryArchiveFormat.GZIP;
    String prefix = format.uriSchemePrefix();
    List<URI> keys = new ArrayList<>();

    URI withEntry = URI.create(prefix + "/tmp/a.gz!//inner/x.txt");
    FileSystemNotFoundException first =
        assertThrows(
            FileSystemNotFoundException.class,
            () ->
                CompressorArchiveProviderSupport.pathFromUri(
                    withEntry,
                    format,
                    key -> {
                      keys.add(key);
                      return null;
                    }));
    assertEquals(withEntry.toString(), first.getMessage());

    URI withoutEntry = URI.create(prefix + "/tmp/b.gz");
    FileSystemNotFoundException second =
        assertThrows(
            FileSystemNotFoundException.class,
            () ->
                CompressorArchiveProviderSupport.pathFromUri(
                    withoutEntry,
                    format,
                    key -> {
                      keys.add(key);
                      return null;
                    }));
    assertEquals(withoutEntry.toString(), second.getMessage());

    assertEquals(2, keys.size());
    assertEquals(URI.create(prefix + Path.of("/tmp/a.gz").toAbsolutePath()), keys.get(0));
    assertEquals(URI.create(prefix + Path.of("/tmp/b.gz").toAbsolutePath()), keys.get(1));
  }
}
