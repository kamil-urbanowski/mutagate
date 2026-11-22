package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

final class GzipFileSystemProviderMutaGate9Test {
  @Test
  void getScheme_returnsLowercaseGzip_andIsStable() {
    GzipFileSystemProvider provider = new GzipFileSystemProvider();
    String scheme = provider.getScheme();

    assertEquals("gzip", scheme);
    assertEquals(scheme, new GzipFileSystemProvider().getScheme());
    assertEquals(scheme.toLowerCase(java.util.Locale.ROOT), scheme);
    assertEquals(scheme.trim(), scheme);
    assertNotEquals("bzip2", scheme);
    assertNotEquals("file", scheme);
  }
}
