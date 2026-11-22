package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class GzipFileSystemProviderMutaGate7Test {
  @Test
  void getScheme_returnsGzip() {
    GzipFileSystemProvider provider = new GzipFileSystemProvider();
    assertEquals("gzip", provider.getScheme());
  }
}
