package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class ArchivePathSupportMutaGate6Test {

  @Test
  void pathSegmentsStripsTrailingSlashes() {
    assertArrayEquals(new String[] {"a", "b"}, ArchivePathSupport.pathSegments("a/b/"));
    assertArrayEquals(new String[] {"dir", "sub"}, ArchivePathSupport.pathSegments("/dir/sub//"));
  }
}
