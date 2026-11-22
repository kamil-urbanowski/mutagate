package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate7Test {

  @Test
  void bangAtStartOfPathIsStripped() {
    URI uri = URI.create("!/entry");
    assertEquals("!/entry", uri.getPath());

    Path result = ArchiveUriSupport.extractArchivePath(uri, null, "tar");

    assertEquals(Path.of(""), result);
    assertEquals("", result.toString());
  }
}
