package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate1Test {

  @Test
  void creationTimeIsNotNullAndMatchesExpectedValue() {
    TarBasicFileAttributes implied = TarBasicFileAttributes.impliedDirectory("dir/");
    assertNotNull(implied.creationTime());
    assertEquals(FileTime.fromMillis(0L), implied.creationTime());
  }
}
