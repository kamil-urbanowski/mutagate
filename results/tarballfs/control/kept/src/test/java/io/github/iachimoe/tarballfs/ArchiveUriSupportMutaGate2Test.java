package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate2Test {

  @Test
  void extractArchivePathBehaviors() {
    // env "path" takes precedence over URI
    Map<String, Object> env = new HashMap<>();
    env.put("path", Path.of("/env/path.tar"));
    env.put("file", Path.of("/env/file.tar"));
    assertEquals(
        Path.of("/env/path.tar"),
        ArchiveUriSupport.extractArchivePath(URI.create("tar:///uri/a.tar"), env, "tar"));

    // falls back to "file" key, with string value converted to Path
    Map<String, Object> fileEnv = new HashMap<>();
    fileEnv.put("file", "/env/fileonly.tar");
    assertEquals(
        Path.of("/env/fileonly.tar"),
        ArchiveUriSupport.extractArchivePath(URI.create("tar:///uri/a.tar"), fileEnv, "tar"));

    // env without path/file falls through to URI
    Map<String, Object> otherEnv = new HashMap<>();
    otherEnv.put("other", "x");
    assertEquals(
        Path.of("/uri/a.tar"),
        ArchiveUriSupport.extractArchivePath(URI.create("tar:///uri/a.tar"), otherEnv, "tar"));

    // null env uses URI path
    assertEquals(
        Path.of("/uri/a.tar"),
        ArchiveUriSupport.extractArchivePath(URI.create("tar:///uri/a.tar"), null, "tar"));

    // bang suffix is stripped
    assertEquals(
        Path.of("/uri/a.tar"),
        ArchiveUriSupport.extractArchivePath(
            URI.create("tar:///uri/a.tar!/dir/entry.txt"), null, "tar"));

    // opaque URI has null path: throws with label and URI in message
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> ArchiveUriSupport.extractArchivePath(URI.create("mailto:foo"), null, "LBL"));
    assertEquals("LBL URI does not contain a file path: mailto:foo", ex.getMessage());
  }
}
