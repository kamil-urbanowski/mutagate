package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class ArchivePathSupportMutaGate2Test {

  @Test
  void pathSegmentsNormalizesInput() {
    assertArrayEquals(new String[0], ArchivePathSupport.pathSegments(null));
    assertArrayEquals(new String[0], ArchivePathSupport.pathSegments(""));
    assertArrayEquals(new String[0], ArchivePathSupport.pathSegments("///"));
    assertArrayEquals(new String[0], ArchivePathSupport.pathSegments("\\\\"));
    assertArrayEquals(new String[] {"a"}, ArchivePathSupport.pathSegments("a"));
    assertArrayEquals(new String[] {"a", "b", "c"}, ArchivePathSupport.pathSegments("a/b/c"));
    assertArrayEquals(new String[] {"a", "b"}, ArchivePathSupport.pathSegments("/a/b/"));
    assertArrayEquals(new String[] {"a", "b"}, ArchivePathSupport.pathSegments("//a//b//"));
    assertArrayEquals(new String[] {"a", "b", "c"}, ArchivePathSupport.pathSegments("a\\b\\c"));
    assertArrayEquals(new String[] {"a", "b", "c"}, ArchivePathSupport.pathSegments("\\a/b\\\\c/"));
    assertArrayEquals(new String[] {"a", " ", "b"}, ArchivePathSupport.pathSegments("a/ /b"));
  }
}
