package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class ArchivePathSupportMutaGate1Test {

  @Test
  void pathSegmentsDropsEmptyInteriorSegments() {
    assertArrayEquals(
        new String[] {"a", "b", "c"}, ArchivePathSupport.pathSegments("a//b///c"));
  }
}
