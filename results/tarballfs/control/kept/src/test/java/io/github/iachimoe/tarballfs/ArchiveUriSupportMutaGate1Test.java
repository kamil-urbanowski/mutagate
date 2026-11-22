package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ArchiveUriSupportMutaGate1Test {

  @Test
  void pathFromEnvResolvesPathFileAndFallbacks() {
    // null env
    assertNull(ArchiveUriSupport.pathFromEnv(null));

    // empty env
    assertNull(ArchiveUriSupport.pathFromEnv(new HashMap<String, Object>()));

    // unrelated keys only
    Map<String, Object> other = new HashMap<>();
    other.put("other", "/x");
    assertNull(ArchiveUriSupport.pathFromEnv(other));

    // Path instance under "path" is returned as the same object
    Path p = Path.of("/tmp/a.tar");
    Map<String, Object> m1 = new HashMap<>();
    m1.put("path", p);
    assertSame(p, ArchiveUriSupport.pathFromEnv(m1));

    // String under "path" is converted
    Map<String, Object> m2 = new HashMap<>();
    m2.put("path", "/tmp/b.tar");
    assertEquals(Path.of("/tmp/b.tar"), ArchiveUriSupport.pathFromEnv(m2));

    // "file" key used when "path" absent
    Map<String, Object> m3 = new HashMap<>();
    m3.put("file", "/tmp/c.tar");
    assertEquals(Path.of("/tmp/c.tar"), ArchiveUriSupport.pathFromEnv(m3));

    // "file" with Path instance returned as same object
    Path fp = Path.of("/tmp/d.tar");
    Map<String, Object> m4 = new HashMap<>();
    m4.put("file", fp);
    assertSame(fp, ArchiveUriSupport.pathFromEnv(m4));

    // "path" takes precedence over "file"
    Map<String, Object> m5 = new HashMap<>();
    m5.put("path", "/tmp/first.tar");
    m5.put("file", "/tmp/second.tar");
    assertEquals(Path.of("/tmp/first.tar"), ArchiveUriSupport.pathFromEnv(m5));

    // "path" explicitly null falls back to "file"
    Map<String, Object> m6 = new HashMap<>();
    m6.put("path", null);
    m6.put("file", "/tmp/fallback.tar");
    assertEquals(Path.of("/tmp/fallback.tar"), ArchiveUriSupport.pathFromEnv(m6));

    // non-String, non-Path object is converted via toString
    Map<String, Object> m7 = new HashMap<>();
    m7.put("path", new StringBuilder("/tmp/sb.tar"));
    assertEquals(Path.of("/tmp/sb.tar"), ArchiveUriSupport.pathFromEnv(m7));
  }
}
