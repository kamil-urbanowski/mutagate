package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class Bzip2FileSystemProviderMutaGate7Test {
  @Test
  void getScheme_returnsBzip2() {
    Bzip2FileSystemProvider provider = new Bzip2FileSystemProvider();
    assertEquals("bzip2", provider.getScheme());
  }
}
