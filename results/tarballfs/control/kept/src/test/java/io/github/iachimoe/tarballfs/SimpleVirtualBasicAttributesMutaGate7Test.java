package io.github.iachimoe.tarballfs;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SimpleVirtualBasicAttributesMutaGate7Test {

  @Test
  void isDirectoryIsTrueOnlyForDirectoriesAndExclusiveOfOtherTypeFlags() {
    SimpleVirtualBasicAttributes dir = SimpleVirtualBasicAttributes.directory("dir/");
    SimpleVirtualBasicAttributes emptyFile = SimpleVirtualBasicAttributes.regularFile("a.txt", 0L);
    SimpleVirtualBasicAttributes bigFile = SimpleVirtualBasicAttributes.regularFile("b.txt", 1024L);

    assertTrue(dir.isDirectory());
    assertFalse(dir.isRegularFile());
    assertFalse(dir.isSymbolicLink());
    assertFalse(dir.isOther());

    assertFalse(emptyFile.isDirectory());
    assertTrue(emptyFile.isRegularFile());

    assertFalse(bigFile.isDirectory());
    assertTrue(bigFile.isRegularFile());
  }
}
