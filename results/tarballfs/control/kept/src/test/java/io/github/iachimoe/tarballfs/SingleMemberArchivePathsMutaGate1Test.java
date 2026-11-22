package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SingleMemberArchivePathsMutaGate1Test {

  @Test
  void pathExistsBehavior() {
    // null and empty refer to the root, which exists
    assertTrue(SingleMemberArchivePaths.pathExists(null));
    assertTrue(SingleMemberArchivePaths.pathExists(""));

    // single-segment names map to the single member
    assertTrue(SingleMemberArchivePaths.pathExists("file.txt"));
    assertTrue(SingleMemberArchivePaths.pathExists("a"));
    assertTrue(SingleMemberArchivePaths.pathExists(" "));

    // any slash, anywhere, means nested/non-single-segment and does not exist
    assertFalse(SingleMemberArchivePaths.pathExists("/"));
    assertFalse(SingleMemberArchivePaths.pathExists("/file.txt"));
    assertFalse(SingleMemberArchivePaths.pathExists("file.txt/"));
    assertFalse(SingleMemberArchivePaths.pathExists("dir/file.txt"));
    assertFalse(SingleMemberArchivePaths.pathExists("a/b/c"));
  }
}
