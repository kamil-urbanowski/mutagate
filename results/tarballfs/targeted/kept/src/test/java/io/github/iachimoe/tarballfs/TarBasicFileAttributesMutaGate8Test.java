package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.attribute.FileTime;

import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate8Test {

  @Test
  void lastAccessTimeIsReturnedAndMatchesEpochForImpliedDirectory() {
    TarBasicFileAttributes attrs = TarBasicFileAttributes.impliedDirectory("some/key");

    FileTime access = attrs.lastAccessTime();

    assertNotNull(access);
    assertEquals(FileTime.fromMillis(0L), access);
  }
}
