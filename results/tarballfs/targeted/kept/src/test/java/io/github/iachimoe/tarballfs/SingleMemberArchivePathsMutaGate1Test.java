package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SingleMemberArchivePathsMutaGate1Test {

  @Test
  void pathExistsHandlesSlashPositions() {
    assertTrue(SingleMemberArchivePaths.pathExists("name"));
    assertFalse(SingleMemberArchivePaths.pathExists("/name"));
    assertFalse(SingleMemberArchivePaths.pathExists("dir/name"));
  }
}
