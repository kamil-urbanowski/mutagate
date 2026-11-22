package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;

class TarBasicFileAttributesMutaGate9Test {

  @Test
  void lastModifiedTimeIsReturned() {
    TarBasicFileAttributes attrs = TarBasicFileAttributes.impliedDirectory("dir/");

    FileTime lastModified = attrs.lastModifiedTime();

    assertNotNull(lastModified);
    assertEquals(FileTime.fromMillis(0L), lastModified);
  }
}
